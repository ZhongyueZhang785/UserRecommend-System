import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{array, col, collect_list, collect_set, concat, explode, flatten, lit, lower, map_from_entries, rand, regexp_replace, round, row_number, size, struct, to_json, to_timestamp, udf, when}
import org.codehaus.jackson.map.ObjectMapper
//import spark.implicits._

//val ACCESS_KEY_ID = dbutils.secrets.get(scope = "aws", key = "aws-access-key-id")
//val SECRET_ACCESS_KEY = dbutils.secrets.get(scope = "aws", key = "aws-secret-access-key")
//sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", ACCESS_KEY_ID)
//sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", SECRET_ACCESS_KEY)
//spark.conf.set("spark.sql.legacy.timeParserPolicy", "LEGACY")

object test {
  def main(args: Array[String]): Unit = {
    val a = "12345"
    val b = "100000000000002"
    println(a <= b)
  }
}