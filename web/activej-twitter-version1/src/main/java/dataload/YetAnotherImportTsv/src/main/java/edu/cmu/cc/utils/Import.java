package edu.cmu.cc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.avro.data.Json;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class Import {
    /**
     * The column header as per the dataset.
     */
    final static String[] COLUMN_HEADER = {
            "create_time",
            "text",
            "reply_to_id",//maybe null
            "retweet_to_id",//maybe null
            "user_id",
            "tweet_id",
            "hash_tag"};
    final static String TABLE_NAME = "tweet";
    /**
     * Byte representation of column family.
     */
    private static byte[] bColFamily = Bytes.toBytes("data");
    /**
     * HTable handler.
     */
    private static Table bizTabletweet;
    /**
     * HTable handler.
     */
    private static Table bizTableuser;
    /**
     * The name of your HBase table.
     */
    private static TableName tableNametweet = TableName.valueOf("tweet");
    /**
     * The name of your HBase table.
     */
    private static TableName tableNameuser = TableName.valueOf("user");
    /**
     * The tweet connectionHBase table.
     */
    private static Connection tweetConnection;

    private static Configuration conf;


    public static Put addTweet(String create_time,String text,String reply_to_id,String retweet_to_id,String user_id,String hash_tag, String rowKey) {
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(bColFamily, Bytes.toBytes("user_id"), Bytes.toBytes(user_id));
        put.addColumn(bColFamily, Bytes.toBytes("text"), Bytes.toBytes(text));
        put.addColumn(bColFamily, Bytes.toBytes("reply_to_id"), Bytes.toBytes(reply_to_id));
        put.addColumn(bColFamily, Bytes.toBytes("retweet_to_id"), Bytes.toBytes(retweet_to_id));
        put.addColumn(bColFamily, Bytes.toBytes("hash"), Bytes.toBytes(hash_tag));
        put.addColumn(bColFamily, Bytes.toBytes("create_time"), Bytes.toBytes(create_time));
        return put;
    }

    public static void init() throws IOException {
        conf = HBaseConfiguration.create();
        String zkAddr = "174.129.45.86";
        conf.set("hbase.master", zkAddr + ":14000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
        conf.set("hbase.zookeeper.property.clientport", "2181");
        tweetConnection = ConnectionFactory.createConnection(conf);

    }
    public static void close() throws IOException {
        if(null!=tweetConnection){
            tweetConnection.close();
        }
    }
    public static void main(String[] args) throws IOException {
//        String inputPath = "/home/hadoop/tweetJson";
//        init();
//        bizTabletweet = tweetConnection.getTable(tableNametweet);
//        try (Scanner sc = new Scanner(new FileReader(inputPath))) {
//            while (sc.hasNextLine()) {  //read by line
//                String line = sc.nextLine();
//                Gson gson = new Gson();
//                int salt = (int)(Math.random()*(100));
//                TweetInput tweetinput = gson.fromJson(line, TweetInput.class);
//                String rowKey = tweetinput.getTweet_id();
//                rowKey = salt+"~"+rowKey;
//                String create_time = tweetinput.getCreate_time();
//                String text = tweetinput.getText();
//                String reply_to_id = tweetinput.getReply_to_id();
//                String retweet_to_id = tweetinput.getRetweet_to_id();
//                String user_id = tweetinput.getUser_id();
//                String tweet_id = tweetinput.getTweet_id();
//                String hash_tag = tweetinput.getHash_tag();
//                if(reply_to_id==null)
//                    reply_to_id = "NULL";
//                if(retweet_to_id==null)
//                    retweet_to_id = "NULL";
//                System.out.println("rowKey: "+rowKey);
//
////                System.out.println("create_time: "+create_time);
////                System.out.println("text: "+text);
////                System.out.println("reply_to_id: "+reply_to_id);
////                System.out.println("retweet_to_id: "+retweet_to_id);
////                System.out.println("user_id: "+user_id);
////                System.out.println("tweet_id："+tweet_id);
////                System.out.println("hash_tag: "+hash_tag);
//                Put put = addTweet(create_time,text,reply_to_id,retweet_to_id,user_id,hash_tag, rowKey);
//                bizTabletweet.put(put);
//                bizTabletweet.close();
//            }
//        }
//        //close();





    }
}

