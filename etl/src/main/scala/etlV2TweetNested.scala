/*
* Schema design for HBase

Tweet: [tweet_id reverse as RowKey]
create_time, tweet_id, user_id, reply_to_id, retweet_to_id, text, hash_tag

User: [user_id reverse as RowKey, hash_tag exclude popular_hashtags]
user_id, screen_name, description, hash_tag, reply_to_user_id_list, retweet_to_user_id_list

Contact: [(user1_id | user2_id) reverse as RowKey, user1_id <= user2_id]
user1_id, user2_id, interaction_score, reply_count, reply_tweet_list, retweet_count, retweet_tweet_list
* */


import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.codehaus.jackson.map.ObjectMapper

import scala.collection.mutable


object etlV2TweetNested {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("etlApp").getOrCreate()
    import spark.implicits._

    // user defined function: to count the occurrence of hashtag (ignore case), return as {"hashtag1":2,"hashtag2":4}
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

    // user defined function:
    def arrayToString(arr: Seq[String]): String = {
      val arrayBuilder = mutable.ArrayBuilder.make[String]
      for (elem <- arr) {
        arrayBuilder += elem
      }
      val stringArray = arrayBuilder.result()
      val stringJoin = stringArray.mkString(",")
      "[" + stringJoin + "]"
    }

    val arrayToStringUdf = udf(arrayToString(_: Seq[String]): String)

    // popular hashtag
    val popularHashTagDF = spark.read.option("header", "false")
      .csv("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/popular_hashtags.txt")
      .toDF("hash_tag")
      .withColumn("hash_tag", lower(col("hash_tag")))
    val popularHashTags = popularHashTagDF.collect().map(x => x(0).toString)

    //    val df = spark.read.json("wasb://datasets@clouddeveloper.blob.core.windows.net/twitter-dataset/*")
    val df = spark.read.json("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/part-r-00000.gz")
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

    // tag count
    //    val tagCount = renamedDF
    //      .withColumn("hash_tag", explode(col("hash_tag")))
    //      .withColumn("hash_tag", lower(col("hash_tag")))
    //      .groupBy("hash_tag")
    //      .count()
    //      .orderBy(col("count").desc)
    //    tagCount.coalesce(1)
    //      .write
    //      .option("compression", "gzip")
    //      .option("delimiter", ",")
    //      .option("encoding", "UTF-8")
    //      .csv("wasb://datasets@jacketlhdistorage.blob.core.windows.net/hash_count")

    // count hash_tag for each row
    val hashtagCountDF = renamedDF
      .select("tweet_id", "hash_tag")
      .withColumn("hash_tag", arrayCountMapUdf(col("hash_tag")))

    val renamedDropHashtagDF = renamedDF.select("create_time", "tweet_id", "text", "reply_to_id", "retweet_to_id", "user_id")
    val tweetDF = renamedDropHashtagDF.join(hashtagCountDF, renamedDropHashtagDF.col("tweet_id") === hashtagCountDF.col("tweet_id"), "outer")
      .drop(hashtagCountDF.col("tweet_id"))
      .withColumn("HBASE_ROW_KEY", reverse(col("tweet_id")))
      .cache()

    // Tweet Table
    // [rowkey(reverse(tweet_id)), tweet_id, user_id, text, reply_to_id, retweet_to_id, hash_tag, create_time]
    tweetDF
      .select("HBASE_ROW_KEY", "create_time", "tweet_id", "user_id", "reply_to_id", "retweet_to_id", "text", "hash_tag")
      .withColumn("text", charEncodeUdf(col("text")))
      .coalesce(1)
      .write
      .mode(SaveMode.Overwrite)
      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
      .option("compression", "gzip")
      .option("delimiter", "\t")
      .option("header", "false")
      .option("encoding", "UTF-8")
      .csv("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/tweet_csv")
    //    tweetDF
    //      .coalesce(1)
    //      .write
    //      .mode(SaveMode.Overwrite)
    //      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
    //      .option("compression", "gzip")
    //      .json("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/tweet_json")

    // User Table
    // [rowkey(reverse(user_id)), user_id, screen_name, description, hash_tag, reply_to_user_id_list,
    // retweet_to_user_id_list]

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
      .filter(!col("hash_tag").isin(popularHashTags: _*))

    // count hash_tag as ["tag" -> 3] format
    val aggDF = latestUserDF.groupBy($"user_id", $"hash_tag").count()
      .groupBy($"user_id")
      .agg(
        map_from_entries(
          collect_list(
            when($"hash_tag".isNotNull, struct($"hash_tag", $"count"))
          )
        ).as("hash_tag")
      ).withColumnRenamed("user_id", "agg_user_id")

    // to generate reply_to_user_id_list, reverse once so that reply_to_user_id_list is bidirectional
    // user_id -> reply_to_id
    // reply_to_id -> user_id
    // [user_id, reply_to_user_id_list]
    val replyTweetDFForward = tweetDF
      .select("user_id", "reply_to_id")
      .filter(col("reply_to_id").isNotNull).cache()
    val replyTweetDFReverse = replyTweetDFForward
      .withColumn("new_user_id", col("reply_to_id"))
      .withColumn("new_reply_to_id", col("user_id"))
      .drop("user_id", "reply_to_id")
      .withColumnRenamed("new_user_id", "user_id")
      .withColumnRenamed("new_reply_to_id", "reply_to_id")
    val replyTweetDF = replyTweetDFForward
      .union(replyTweetDFReverse)
      .groupBy("user_id").agg(collect_set("reply_to_id").alias("reply_to_user_id_list"))
      .withColumnRenamed("user_id", "reply_user_id")

    // to generate retweet_to_user_id_list, reverse once so that retweet_to_user_id_list is bidirectional
    // user_id -> retweet_to_id
    // retweet_to_id -> user_id
    // [user_id, retweet_to_user_id_list]
    val retweetTweetDFForward = tweetDF
      .select("user_id", "retweet_to_id")
      .filter(col("retweet_to_id").isNotNull).cache()
    val retweetTweetDFReverse = retweetTweetDFForward
      .withColumn("new_user_id", col("retweet_to_id"))
      .withColumn("new_retweet_to_id", col("user_id"))
      .drop("user_id", "retweet_to_id")
      .withColumnRenamed("new_user_id", "user_id")
      .withColumnRenamed("new_retweet_to_id", "retweet_to_id")
    val retweetTweetDF = retweetTweetDFForward
      .union(retweetTweetDFReverse)
      .groupBy("user_id").agg(collect_set("retweet_to_id").alias("retweet_to_user_id_list"))
      .withColumnRenamed("user_id", "retweet_user_id")

    // join the latest user info with the calculated hash tag count
    val latestUserDF1 = allUserInfoDF.select("user_id", "description", "screen_name")
    val userDFAgg = latestUserDF1.join(aggDF, latestUserDF1.col("user_id") === aggDF.col("agg_user_id"), "left_outer")
      .drop("agg_user_id")
      .withColumn("hash_tag", to_json(col("hash_tag")))
      .withColumn("hash_tag", col("hash_tag").cast("String"))
      .na.fill("{}", Seq("hash_tag"))

    val userDFReplyJoin = userDFAgg.join(replyTweetDF, userDFAgg.col("user_id") === replyTweetDF.col("reply_user_id"), "left_outer")
      .drop("reply_user_id")

    val userDFRetweetJoin = userDFReplyJoin.join(retweetTweetDF, userDFReplyJoin.col("user_id") === retweetTweetDF.col("retweet_user_id"), "left_outer")
      .drop("retweet_user_id")

    val userDF = userDFRetweetJoin
      .withColumn("reply_to_user_id_list", when(
        col("reply_to_user_id_list").isNull, array()
      ).otherwise(col("reply_to_user_id_list")))
      .withColumn("retweet_to_user_id_list", when(
        col("retweet_to_user_id_list").isNull, array()
      ).otherwise(col("retweet_to_user_id_list")))
      .withColumn("HBASE_ROW_KEY", reverse(col("user_id")))

    userDF
      .select("HBASE_ROW_KEY", "user_id", "screen_name", "description", "hash_tag", "reply_to_user_id_list", "retweet_to_user_id_list")
      .withColumn("reply_to_user_id_list", arrayToStringUdf(col("reply_to_user_id_list")))
      .withColumn("retweet_to_user_id_list", arrayToStringUdf(col("retweet_to_user_id_list")))
      .withColumn("description", charEncodeUdf(col("description")))
      .coalesce(1)
      .write
      .mode(SaveMode.Overwrite)
      .option("compression", "gzip")
      .option("delimiter", "\t")
      .option("header", "false")
      .csv("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/user_csv")
    //    userDF
    //      .coalesce(1)
    //      .write
    //      .mode(SaveMode.Overwrite)
    //      .option("compression", "gzip")
    //      .json("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/user_json")


    // [Temp] Reply Table: compare the user_id and reply_to_id, let the smaller one in (user_id, reply_to_id)
    //        let the smaller one in (user_id, reply_to_id) to be user1_id, and the other bigger one to be user2_id
    // [user1_id, user2_id, tweet_id]
    val replyRDD = tweetDF
      .select("user_id", "reply_to_id", "tweet_id", "text", "hash_tag", "create_time")
      .filter(col("reply_to_id").isNotNull)
      .rdd.map(row => (row(0), row(1), row(2), row(3), row(4), row(5)))
      .map(x => {
        //        val user_id = BigInt(x._1.toString)
        //        val reply_to_id = BigInt(x._2.toString)
        val user_id = x._1.toString
        val reply_to_id = x._2.toString
        if (user_id <= reply_to_id) {
          (x._1, x._2, x._3, x._4, x._5, x._6)
        } else {
          (x._2, x._1, x._3, x._4, x._5, x._6)
        }
      })
      .map(x => (x._1.toString, x._2.toString, x._3.toString, x._4.toString, x._5.toString, x._6.toString))
    val replyDF = replyRDD
      .toDF("reply_user1_id", "reply_user2_id", "tweet_id", "text", "hash_tag", "create_time")
      .withColumn("text", charEncodeUdf(col("text")))

    // group by user1_id and user2_id, generate reply_count and reply_tweet_list
    // [user1_id, user2_id, reply_count, reply_tweet_list]
    val replyDFAgg = replyDF.groupBy("reply_user1_id", "reply_user2_id").agg(
      count("tweet_id").alias("reply_count"),
      collect_list(
        to_json(struct(col("tweet_id"), col("text"), col("hash_tag"), col("create_time")))
      ).alias("reply_tweet_list")
    ).cache()

    // [Temp] Retweet Table: compare the user_id and retweet_to_id, let the smaller one in (user_id, retweet_to_id)
    //        let the smaller one in (user_id, retweet_to_id) to be user1_id, and the other bigger one to be user2_id
    // [user1_id, user2_id, tweet_id]
    val retweetRDD = tweetDF
      .select("user_id", "retweet_to_id", "tweet_id", "text", "hash_tag", "create_time")
      .filter(col("retweet_to_id").isNotNull)
      .rdd.map(row => (row(0), row(1), row(2), row(3), row(4), row(5)))
      .map(x => {
        //        val user_id = BigInt(x._1.toString)
        //        val reply_to_id = BigInt(x._2.toString)
        val user_id = x._1.toString
        val reply_to_id = x._2.toString
        if (user_id <= reply_to_id) {
          (x._1, x._2, x._3, x._4, x._5, x._6)
        } else {
          (x._2, x._1, x._3, x._4, x._5, x._6)
        }
      })
      .map(x => (x._1.toString, x._2.toString, x._3.toString, x._4.toString, x._5.toString, x._6.toString))
    val retweetDF = retweetRDD
      .toDF("retweet_user1_id", "retweet_user2_id", "tweet_id", "text", "hash_tag", "create_time")
      .withColumn("text", charEncodeUdf(col("text")))

    // group by user1_id and user2_id, generate retweet_count and retweet_tweet_list
    // [user1_id, user2_id, retweet_count, retweet_tweet_list]
    val retweetDFAgg = retweetDF.groupBy("retweet_user1_id", "retweet_user2_id").agg(
      count("tweet_id").alias("retweet_count"),
      collect_list(
        to_json(struct(col("tweet_id"), col("text"), col("hash_tag"), col("create_time")))
      ).alias("retweet_tweet_list")
    ).cache()

    // Contact Table
    // [rowkey(reverse(user1_id|user2_id)), user1_id, user2_id, reply_count, reply_tweet_list, retweet_count,
    // retweet_tweet_list, interaction_score]
    val contactUserDF = replyDFAgg
      .withColumnRenamed("reply_user1_id", "user1_id")
      .withColumnRenamed("reply_user2_id", "user2_id")
      .select("user1_id", "user2_id")
      .union(retweetDFAgg
        .withColumnRenamed("retweet_user1_id", "user1_id")
        .withColumnRenamed("retweet_user2_id", "user2_id")
        .select("user1_id", "user2_id")
      ).distinct()

    val contactDFJoinReply = contactUserDF.join(
      replyDFAgg,
      contactUserDF.col("user1_id") === replyDFAgg.col("reply_user1_id")
        && contactUserDF.col("user2_id") === replyDFAgg.col("reply_user2_id"),
      "left_outer"
    )

    val contactDF = contactDFJoinReply.join(
      retweetDFAgg,
      contactDFJoinReply.col("user1_id") === retweetDFAgg.col("retweet_user1_id")
        && contactDFJoinReply.col("user2_id") === retweetDFAgg.col("retweet_user2_id"),
      "left_outer"
    ).drop("reply_user1_id", "reply_user2_id", "retweet_user1_id", "retweet_user2_id")
      .na.fill(0, Seq("reply_count"))
      .na.fill(0, Seq("retweet_count"))
      .withColumn("reply_tweet_list", when(
        col("reply_tweet_list").isNull, array()
      ).otherwise(col("reply_tweet_list")))
      .withColumn("retweet_tweet_list", when(
        col("retweet_tweet_list").isNull, array()
      ).otherwise(col("retweet_tweet_list")))
      .withColumn("interaction_score", log(lit(1) + lit(2) * col("reply_count") + col("retweet_count")))
      .withColumn("HBASE_ROW_KEY", reverse(concat(col("user1_id"), lit("~"), col("user2_id"))))

    contactDF
      //      .select("HBASE_ROW_KEY", "user1_id", "user2_id", "interaction_score", "reply_count", "reply_tweet_list", "retweet_count", "retweet_tweet_list")
      .select("HBASE_ROW_KEY", "interaction_score", "reply_tweet_list", "retweet_tweet_list")
      .withColumn("reply_tweet_list", arrayToStringUdf(col("reply_tweet_list")))
      .withColumn("retweet_tweet_list", arrayToStringUdf(col("retweet_tweet_list")))
      .coalesce(1)
      .write
      .option("compression", "gzip")
      .option("delimiter", "\t")
      .option("header", "false")
      .option("encoding", "UTF-8")
      .csv("file:/D:/Jack/PycharmProjects/NoeatNosleep2021-F21/etl/src/main/resources/contact_csv")
  }
}

