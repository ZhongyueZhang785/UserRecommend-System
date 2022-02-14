// Databricks notebook source

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{array, col, collect_list, collect_set, concat, explode, flatten, lit, lower, map_from_entries, rand, regexp_replace, round, row_number, size, struct, to_json, to_timestamp, udf, when}
import org.codehaus.jackson.map.ObjectMapper
import spark.implicits._

// COMMAND ----------

val ACCESS_KEY_ID = dbutils.secrets.get(scope = "aws", key = "aws-access-key-id")
val SECRET_ACCESS_KEY = dbutils.secrets.get(scope = "aws", key = "aws-secret-access-key")
sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", ACCESS_KEY_ID)
sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", SECRET_ACCESS_KEY)
spark.conf.set("spark.sql.legacy.timeParserPolicy", "LEGACY")

// COMMAND ----------

def arrayCountMap(arr: Seq[String]): String = {
  var hashMap = new java.util.HashMap[String, Int]()
  for (elem <- arr) {
    val lowerElem = elem.toLowerCase
    var currentCount = hashMap.getOrDefault(lowerElem, 0)
    hashMap.put(lowerElem, currentCount + 1)
  }
  val mapper = new ObjectMapper()
  mapper.writeValueAsString(hashMap)
}

val arrayCountMapUdf = udf(arrayCountMap(_: Seq[String]): String)

// user defined function:
def charEncode(str: String): String = {
  if (str != null) {
    var s1 = str.replaceAll("\n", """\\n""")
    s1 = s1.replaceAll("\r", """\\r""")
    s1
  }
  else {
    ""
  }
}

val charEncodeUdf = udf(charEncode(_: String): String)

// COMMAND ----------

val df = spark.read.json("s3://cmucc-datasets/twitter/f21-mini")
//     val df = spark.read.json("wasb://datasets@clouddeveloper.blob.core.windows.net/twitter-dataset/part-r-00000.gz")
val langAllowed = List("ar", "en", "fr", "in", "pt", "es", "tr", "ja")
val filteredDF = df.filter(col("id").isNotNull && col("id_str").isNotNull &&
  col("user.id").isNotNull && col("user.id_str").isNotNull &&
  col("created_at").isNotNull && col("text").isNotNull && col("text").notEqual("") &&
  col("user.id").isNotNull && col("entities.hashtags").isNotNull &&
  col("lang").isInCollection(langAllowed) && size(col("entities.hashtags")) > 0)
  .withColumn("created_at", to_timestamp(col("created_at"), "E MMM dd HH:mm:ss Z yyyy"))
// select needed columns
val renamedDF = filteredDF.withColumn("retweet_to_id", col("retweeted_status.user.id_str"))
  .withColumn("user_id", col("user.id_str"))
  .withColumn("hash_tag", col("entities.hashtags.text"))
  .withColumnRenamed("in_reply_to_user_id_str", "reply_to_id")
  .withColumnRenamed("created_at", "create_time")
  .withColumnRenamed("id_str", "tweet_id").cache()

// count hash_tag for each row
val hashtagCountDF = renamedDF
  .select("tweet_id", "hash_tag")
  .withColumn("hash_tag", arrayCountMapUdf(col("hash_tag")))

val renamedDropHashtagDF = renamedDF.select("create_time", "tweet_id", "text", "reply_to_id", "retweet_to_id", "user_id")
val tweetDF = renamedDropHashtagDF.join(hashtagCountDF, renamedDropHashtagDF.col("tweet_id") === hashtagCountDF.col("tweet_id"), "outer")
  .drop(hashtagCountDF.col("tweet_id"))
  .withColumn("HBASE_ROW_KEY", concat(lit((rand() * 100).cast("int")), lit("~"), col("tweet_id")))

// Tweet Table
tweetDF
  .select("HBASE_ROW_KEY", "create_time", "tweet_id", "user_id", "reply_to_id", "retweet_to_id", "text", "hash_tag")
  .withColumn("text", charEncodeUdf(col("text")))
  .write
  .mode(SaveMode.Overwrite)
  .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
  .option("compression", "gzip")
  .option("delimiter", "\t")
  .option("header", "false")
  .option("encoding", "UTF-8")
  .csv("s3://twitter-nens/mini/tweet_csv")
//     tweetDF
//       .coalesce(1)
//       .write
//       .mode(SaveMode.Overwrite)
//       .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
//       .option("compression", "gzip")
//       .json("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/tweet_json")

// User Table
// get the latest user info by created_at_ts and id
val retweetedStatusUserInfoDF = renamedDF
  .drop("user_id") //remove the original tweet user id
  .drop("hash_tag") // remove the original tweet user hash tag
  .drop("tweet_id") // remove the original tweet id
  .withColumn("screen_name", col("retweeted_status.user.screen_name"))
  .withColumn("description", col("retweeted_status.user.description"))
  .withColumn("user_id", col("retweeted_status.user.id_str"))
  .withColumn("tweet_id", col("retweeted_status.id_str"))
  //      .withColumn("hash_tag", lit(array()))
  .select("user_id", "screen_name", "description", "create_time", "tweet_id")
  .filter(col("user_id").isNotNull)

val tweetUserInfoDF = renamedDF
  .withColumn("screen_name", col("user.screen_name"))
  .withColumn("description", col("user.description"))
  .select("user_id", "screen_name", "description", "create_time", "tweet_id")

// gather all user info from tweet user and retweet status user without hash tag
val w1 = Window.partitionBy("user_id").orderBy(col("create_time").desc, col("tweet_id").desc)
val allUserInfoDF = retweetedStatusUserInfoDF
  .union(tweetUserInfoDF)
  .withColumn("row_num_in_window", row_number().over(w1))
  .where(col("row_num_in_window") === 1)
  .drop("row_num_in_window")

val userSelectedDF = renamedDF
  .withColumn("screen_name", col("user.screen_name"))
  .withColumn("description", col("user.description"))
  .select("user_id", "hash_tag", "screen_name", "description", "create_time", "tweet_id")

// explode the hash tag for each of the user and count the hash tag, format as json style
val w2 = Window.partitionBy($"user_id").orderBy($"create_time".desc, $"tweet_id".desc)
val latestUserDF = userSelectedDF.withColumn("row_num", row_number.over(w2))
  .where($"row_num" === 1)
  .drop("row_num")
  .withColumn("hash_tag", explode(col("hash_tag")))
  .withColumn("hash_tag", lower(col("hash_tag")))
// count as ["tag" -> 3] format
val aggDF = latestUserDF.groupBy($"user_id", $"hash_tag").count()
  .groupBy($"user_id")
  .agg(
    map_from_entries(
      collect_list(
        when($"hash_tag".isNotNull, struct($"hash_tag", $"count"))
      )
    ).as("hash_tag")
  )

// join the latest user info with the calculated hash tag count
val latestUserDF1 = allUserInfoDF.select("user_id", "description", "screen_name")
val userDF = latestUserDF1.join(aggDF, latestUserDF1.col("user_id") === aggDF.col("user_id"), "outer")
  .drop(aggDF.col("user_id"))
  .withColumn("hash_tag", to_json(col("hash_tag")))
  .withColumn("hash_tag", col("hash_tag").cast("String"))
  .withColumn("HBASE_ROW_KEY", concat(lit((rand() * 100).cast("int")), lit("~"), col("user_id")))
  .na.fill("{}", Seq("hash_tag"))
userDF
  .select("HBASE_ROW_KEY", "user_id", "screen_name", "description", "hash_tag")
  .withColumn("description", charEncodeUdf(col("description")))
  .write
  .mode(SaveMode.Overwrite)
  .option("compression", "gzip")
  .option("delimiter", "\t")
  .option("header", "false")
  .csv("s3://twitter-nens/mini/user_csv")
//     userDF
//       .coalesce(1)
//       .write
//       .mode(SaveMode.Overwrite)
//       .option("compression", "gzip")
//       .json("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/user_json")
