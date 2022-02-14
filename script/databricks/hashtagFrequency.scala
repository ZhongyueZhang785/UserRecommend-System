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

val df = spark.read.json("wasb://datasets@clouddeveloper.blob.core.windows.net/twitter-dataset/")
//     val df = spark.read.json("s3://cmucc-datasets/twitter/f21-mini")
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

val tp = renamedDF
  .withColumn("hash_tag", explode(col("hash_tag")))
  .withColumn("hash_tag", lower(col("hash_tag")))
  .groupBy("hash_tag")
  .count()
  .orderBy(col("count").desc)
tp.coalesce(1)
  .write
  .option("compression", "gzip")
  .option("delimiter", ",")
  .option("encoding", "UTF-8")
  //       .csv("wasb://datasets@jacketlhdistorage.blob.core.windows.net/hash_count")
  .csv("s3://twitter-nens/hash_count")
