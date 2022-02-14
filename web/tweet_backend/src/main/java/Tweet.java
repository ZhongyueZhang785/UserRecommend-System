

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.protobuf.generated.TableProtos;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.commons.lang3.StringUtils;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Tweet {


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
     * The address for stroing file
     */
    private static String fileName = "src\\main\\java\\popular_hashtags.txt";
    //store the filter hashtags
    private static HashMap<String, Integer> popular_hashtags;

    //Store all result: key user_id/score/screen/description/text
    HashMap<String, UserRow> allMap = new HashMap<String, UserRow>();
    //store the reply tweets
    ArrayList<SendTweet> tweetsReply = new ArrayList<>();
    //store the retweet tweets
    ArrayList<SendTweet> tweetsRetweet = new ArrayList<>();
    String userList="";
    /**
     * Calculate total interaction score
     *
     * @param userid
     * @return
     * @throws IOException
     */
    public Boolean calInteractionScore(String userid) throws IOException {


        Scan scan = new Scan();

        byte[] bCol = Bytes.toBytes("user_id");
        byte[] bCol2 = Bytes.toBytes("reply_to_id");
        byte[] bCol3 = Bytes.toBytes("retweet_to_id");
        scan.addColumn(bColFamily, bCol);
        RegexStringComparator comp = new RegexStringComparator(userid);
        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ONE);//OR
        //create a filter
        Filter filter = new SingleColumnValueFilter(
                bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        Filter filter2 = new SingleColumnValueFilter(
                bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp);
        Filter filter3 = new SingleColumnValueFilter(
                bColFamily, bCol3, CompareFilter.CompareOp.EQUAL, comp);
        list.addFilter(filter);
        list.addFilter(filter2);
        list.addFilter(filter3);
        //scan the table
        scan.setFilter(list);
        ResultScanner rs = bizTabletweet.getScanner(scan);


        for (Result r = rs.next(); r != null; r = rs.next()) {
            String reply_to_id = null;
            String retweet_to_id = null;
            String content = null;
            String hashtag = null;
            String create_time = null;


            byte[] bCol_reply = Bytes.toBytes("reply_to_id");
            byte[] bCol_user = Bytes.toBytes("user_id");
            byte[] bCol_retweet = Bytes.toBytes("retweet_to_id");
            byte[] bCol_hashtag = Bytes.toBytes("hash_tag");
            byte[] bCol_content = Bytes.toBytes("text");
            byte[] bCol_createTime = Bytes.toBytes("create_time");

            hashtag = Bytes.toString(r.getValue(bColFamily, bCol_hashtag));
            content = Bytes.toString(r.getValue(bColFamily, bCol_content));
            create_time = Bytes.toString(r.getValue(bColFamily, bCol_createTime));

            if (r.getValue(bColFamily, bCol_user).toString() == userid) {
                if (r.getValue(bColFamily, bCol_reply) != null) {
                    reply_to_id = Bytes.toString(r.getValue(bColFamily, bCol_reply));
                    addTweetReply(userid, content, reply_to_id, hashtag, create_time);
                    groupby(reply_to_id, "reply");
                }
                if (r.getValue(bColFamily, bCol_retweet) != null) {
                    retweet_to_id = Bytes.toString(r.getValue(bColFamily, bCol_retweet));
                    addTweetRetweet(userid, content, retweet_to_id, hashtag, create_time);
                    groupby(retweet_to_id, "retweet");
                }
            } else {
                if (r.getValue(bColFamily, bCol_reply).toString() == userid) {
                    reply_to_id = Bytes.toString(r.getValue(bColFamily, bCol_user));
                    addTweetReply(userid, content, reply_to_id, hashtag, create_time);
                    groupby(reply_to_id, "reply");
                }
                if (r.getValue(bColFamily, bCol_retweet).toString() == userid) {
                    retweet_to_id = Bytes.toString(r.getValue(bColFamily, bCol_user));
                    addTweetRetweet(userid, content, retweet_to_id, hashtag, create_time);
                    groupby(retweet_to_id, "retweet");
                }
            }

        }

        // Cleanup
        rs.close();
        countInteract();
        if(allMap.size()==0)
            return false;
        else
            return true;

    }

    /**
     * add a tweet to tweetReply Array
     *
     * @param sender_uid
     * @param content
     * @param reply_to_uid
     * @param hashtags
     */
    public void addTweetReply(String sender_uid, String content, String reply_to_uid, String hashtags, String create_time) {
        SendTweet tweet = new SendTweet();
        tweet.setReply(sender_uid, content, reply_to_uid, hashtags, create_time);
        tweetsReply.add(tweet);
    }

    /**
     * add a tweet to tweetRetweet Array
     *
     * @param sender_uid
     * @param content
     * @param retweet_to_uid
     * @param hashtags
     */
    public void addTweetRetweet(String sender_uid, String content, String retweet_to_uid, String hashtags, String create_time) {
        SendTweet tweet = new SendTweet();
        tweet.setRetweet(sender_uid, content, retweet_to_uid, hashtags, create_time);
        tweetsRetweet.add(tweet);
    }


    public void groupby(String id, String type) {
        if (type == "reply") {
            if (allMap.containsKey(id)) {
                allMap.get(id).setReply_num();
            } else {
                UserRow user = new UserRow(id);
                user.setReply_num();
                allMap.put(id, user);
            }
        } else {
            if (allMap.containsKey(id)) {
                allMap.get(id).setRetweet_num();
            } else {
                UserRow user = new UserRow(id);
                user.setRetweet_num();
                allMap.put(id, user);
            }
        }
    }

    /**
     * count final interaction score
     *
     */
    public void countInteract() {
        for (String id : allMap.keySet()) {
            allMap.get(id).calculateInteractionScore();
        }
    }

    public void calHashTagScore(String userid) throws IOException {
        HashMap<String, HashMap<String,Double>> usersHashTag = new   HashMap<String, HashMap<String,Double>>();
        popular_hashtags = readPopularHashtags();
        userList = userid+userList;
        byte[] bCol = Bytes.toBytes("user_id");
        RegexStringComparator comp = new RegexStringComparator(userList);
        Filter filter = new SingleColumnValueFilter(
                bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        Scan scan = new Scan();
        scan.setFilter(filter);
        ResultScanner rs = bizTableuser.getScanner(scan);

        String hashTag = null;
        String get_user_id = null;
        String screen_name = null;
        String description = null;
        byte[] bColHash = Bytes.toBytes("hash_tag");
        byte[] bColUser = Bytes.toBytes("user_id");
        byte[] bColScreenName = Bytes.toBytes("screen_name");
        byte[] bColDescription = Bytes.toBytes("description");
        for (Result r = rs.next(); r != null; r = rs.next()) {
            hashTag = r.getValue(bColFamily, bColHash).toString();
            get_user_id = r.getValue(bColFamily, bColUser).toString();
            screen_name = r.getValue(bColFamily,bColScreenName).toString();
            description = r.getValue(bColFamily,bColDescription).toString();
            allMap.get(get_user_id).setDescriptionAndScreenName(description,screen_name);
            HashMap<String,Double> userHashTagFilter = userHashTag(hashTag);
            usersHashTag.put(get_user_id,userHashTagFilter);
        }
        HashMap<String,Double> requestUserHashTag = usersHashTag.get(userid);
        for (String hashtag : requestUserHashTag.keySet()){
            Double count = requestUserHashTag.get(hashtag);//find the count of request user_id
            for(String id: usersHashTag.keySet()){
                if(id !=userid){
                    if(usersHashTag.get(id).containsKey(hashtag)){// if contact user_id also has the hashtag
                        count = count + usersHashTag.get(id).get(hashtag);// add the count
                        allMap.get(id).setSame_tag_count(count);//store in user
                    }
            }
        }
    }
        allMap.get(userid).setSame_tag_count(1.0);//Set self-retweet and self-reply Score = 1
    }
    public static HashMap<String,Double> userHashTag(String hashTags) throws FileNotFoundException {
        HashMap<String, Object> userHashTag = json2Map(hashTags);
        HashMap<String, Double> userUpdate= new HashMap<>();
        for (String hashtag : userHashTag.keySet()){
            if(!popular_hashtags.containsKey(hashtag)){
                //if the hashtag is not in the popular_hashtags
                double count = (double) userHashTag.get(hashtag);
                userUpdate.put(hashtag, count);
            }

        }
        return userUpdate;

    }

    public static HashMap<String, Integer> readPopularHashtags() throws FileNotFoundException {
        //Waiting for change
        HashMap<String, Integer> popular_hashtags = new HashMap<String, Integer>();
        try (Scanner sc = new Scanner(new FileReader(fileName))) {
            while (sc.hasNextLine()) {  //read by line
                String line = sc.nextLine();
                popular_hashtags.put(line, 1);
            }
        }
        return popular_hashtags;
    }


    public void calInteractionKeyScore(String phrase, String hashtag, String type) {

        if (type.equals("retweet")){
            updateKWCountWithLatest(phrase, hashtag, tweetsRetweet);
            updateKWCountWithout(phrase, hashtag, tweetsReply);
        }
        else if (type.equals("reply")){
            updateKWCountWithLatest(phrase, hashtag, tweetsReply);
            updateKWCountWithout(phrase, hashtag, tweetsRetweet);
        }
        else {
            updateKWCountWithLatest(phrase, hashtag, tweetsRetweet);
            updateKWCountWithLatest(phrase, hashtag, tweetsReply);
        }

        for(Map.Entry<String, UserRow> user : allMap.entrySet()) {
            UserRow userRow = user.getValue();
            userList += userRow.calculateHashtagScore();
        }
    }

    public void updateKWCountWithLatest(String phrase, String hashtag, ArrayList<SendTweet> tweetsReplyOrRetweet){

        for (SendTweet tweet: tweetsReplyOrRetweet){
            String userId = tweet.getReply_to_uid();
            UserRow currentUser=allMap.get(userId);

            if (currentUser != null){
                String content = tweet.getContent();
                String hashTags = tweet.getHashtags();

                // add phrase match:
                int phrase_match = StringUtils.countMatches(content, phrase);
                currentUser.addPhraseMatch(phrase_match);

                // add hashtag match:
                int hashtag_match = 0;
                currentUser.addHashtagMatch(hashtag_match);

                //update lasted contact:
                currentUser.updateTweet(tweet.getContent(),tweet.getCreate_time());
            }
        }

    }

    public void updateKWCountWithout(String phrase, String hashtag, ArrayList<SendTweet> tweetsReplyOrRetweet){

        for (SendTweet tweet: tweetsReplyOrRetweet){
            String userId = tweet.getReply_to_uid();
            UserRow currentUser=allMap.get(userId);

            if (currentUser != null){
                String content = tweet.getContent();
                String hashTags = tweet.getHashtags();

                // add phrase match:
                int phrase_match = StringUtils.countMatches(content, phrase);
                currentUser.addPhraseMatch(phrase_match);

                // add hashtag match:
                int hashtag_match = 0;
                currentUser.addHashtagMatch(hashtag_match);
            }
        }

    }

    public static Table createTable(TableName tablename) throws IOException {
        Configuration configuration = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(configuration);
        Admin admin = connection.getAdmin();
        if (admin.tableExists(tablename)) {//if exist firstly delete than create
            admin.disableTable(tablename);
            admin.deleteTable(tablename);
        }
        HTableDescriptor tableDescriptor = new HTableDescriptor(tablename);
        tableDescriptor.addFamily(new HColumnDescriptor(bColFamily));// adding column families
        admin.createTable(tableDescriptor);
        return connection.getTable(tablename);

    }

    public static Put addTweet(String tweet_id, String user_id, String text, String reply_to_id, String retweet_to_id, String hash_tag, String create_time) {
        Put put = new Put(Bytes.toBytes(tweet_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("user_id"), Bytes.toBytes(user_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("text"), Bytes.toBytes(text));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("reply_to_id"), Bytes.toBytes(reply_to_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("retweet_to_id"), Bytes.toBytes(retweet_to_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("hash_tag"), Bytes.toBytes(hash_tag));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("create_time"), Bytes.toBytes(create_time));
        return put;
    }

    public static Put addUser(String user_id, String hash_tag, String screen_name, String description, String retweet, String reply) {
        Put put = new Put(Bytes.toBytes(user_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("user_id"), Bytes.toBytes(user_id));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("hash_tag"), Bytes.toBytes(hash_tag));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("screen_name"), Bytes.toBytes(screen_name));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("description"), Bytes.toBytes(description));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("retweet"), Bytes.toBytes(retweet));
        put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("reply"), Bytes.toBytes(reply));
        return put;
    }
    public static HashMap<String, Object> json2Map(String str) {
        HashMap<String, Object> map = null;
        try {
            Gson gson = new Gson();
            map = gson.fromJson(str, new TypeToken<HashMap<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
        }
        return map;
    }

    public static void main(String[] args) throws IOException {

//        bizTabletweet = createTable(tableNametweet);
//        bizTableuser = createTable(tableNameuser);
//        //add tweet
//        String tweet_id = "451954866139590656";
//        String user_id = "2401535519";
//        String text =  "25.hellpskaj";
//        String reply_to_id = null;
//        String retweet_to_id = null;
//        String hash_tag = "\u0633\u0643\u0633";
//        String create_time = "Fri Apr 04 05:30:11 +0000 2014";
//
//        bizTabletweet.put(addTweet(tweet_id,user_id, text,reply_to_id,retweet_to_id,hash_tag,create_time));
//        bizTabletweet.close();
//
//        //add user
//        hash_tag = "\u0633\u0643\u0633";
//        String screen_name = "serfearnar3";
//        String description = null;
//        String retweet = "True";
//        String reply = "False";
//        bizTableuser.put(addUser(user_id,hash_tag,screen_name,description,retweet,reply));
//        bizTableuser.close();

        popular_hashtags = readPopularHashtags();

        boolean status = popular_hashtags.containsKey("わーーーーーージャニオタさんと繋がるお時間がまいりましたなのでいっぱい繋がりましょそして濃く絡んで元気なっちゃいましょrtしてくれた方で気になった方お迎えです");
        String hashTag = "{'Monday': 1, 'Tuesday': 2,'love':2}";
        HashMap<String, Double> obj = userHashTag(hashTag);
        System.out.println(obj.toString());


    }
}
