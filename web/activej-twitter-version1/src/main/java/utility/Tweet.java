package utility;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.IFileOutputStream;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

// sorting:

// Descending order
class CompareScoreId implements Comparator<UserRow> {
    public int compare(UserRow u1, UserRow u2) {
        if (Double.compare(u2.getScore(), u1.getScore()) != 0) {
            return Double.compare(u2.getScore(), u1.getScore());
        } else {
            if ((Long.valueOf(u2.getUser_id()) - Long.valueOf(u1.getUser_id()))<0){
                return -1;
            }
            else {
                return 1;
            }
        }
    }
}


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
    private static String fileName = "src\\main\\java\\utility\\popular_hashtags.txt";
    //store the filter hashtags
    private static HashMap<String, Integer> popular_hashtags;

    //Store all result: key user_id/score/screen/description/text
    HashMap<String, UserRow> allMap = new HashMap<String, UserRow>();
    //store the reply tweets
    ArrayList<SendTweet> tweetsReply = new ArrayList<>();
    //store the retweet tweets
    ArrayList<SendTweet> tweetsRetweet = new ArrayList<>();
    String userList = "";

    //constructor:


    public Tweet(Connection tweetConnection, HashMap<String, Integer> popular_hashtags) throws IOException {
        bizTabletweet = tweetConnection.getTable(tableNametweet);
        bizTableuser = tweetConnection.getTable(tableNameuser);
        this.popular_hashtags = popular_hashtags;
    }

    /**
     * Calculate total interaction score
     *
     * @param userid
     * @return
     * @throws IOException
     */
    public Boolean calInteractionScore(String userid) throws IOException {

        Scan scan = new Scan();
        //scan.setCaching(1000);
        byte[] bCol = Bytes.toBytes("user_id");
        byte[] bCol2 = Bytes.toBytes("reply_to_id");
        byte[] bCol3 = Bytes.toBytes("retweet_to_id");

        //RegexStringComparator comp = new RegexStringComparator("^" + userid + "$");
        RegexStringComparator comp = new RegexStringComparator(userid);


        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ONE);//OR
        //create a filter
        Filter filter1 = new SingleColumnValueFilter(
                bColFamily, bCol, CompareFilter.CompareOp.EQUAL, comp);
        Filter filter2 = new SingleColumnValueFilter(
                bColFamily, bCol2, CompareFilter.CompareOp.EQUAL, comp);
        Filter filter3 = new SingleColumnValueFilter(
                bColFamily, bCol3, CompareFilter.CompareOp.EQUAL, comp);
        list.addFilter(filter1);
        list.addFilter(filter2);
        list.addFilter(filter3);
        //scan the table
        scan.setFilter(list);
        long begin = System.nanoTime();
        ResultScanner rs = bizTabletweet.getScanner(scan);


        for (Result r = rs.next(); r != null; r = rs.next()) {
            long end = System.nanoTime();
            String reply_to_id = null;
            String retweet_to_id = null;
            String content = null;
            String hashtag = null;
            String create_time = null;
            String tweet_id = null;

            byte[] bCol_reply = Bytes.toBytes("reply_to_id");
            byte[] bCol_user = Bytes.toBytes("user_id");
            byte[] bCol_retweet = Bytes.toBytes("retweet_to_id");
            byte[] bCol_hashtag = Bytes.toBytes("hash_tag");
            byte[] bCol_content = Bytes.toBytes("text");
            byte[] bCol_createTime = Bytes.toBytes("create_time");
            byte[] bColTweet = Bytes.toBytes("tweet_id");

            hashtag = Bytes.toString(r.getValue(bColFamily, bCol_hashtag));
            hashtag = hashtag.replace("\\\"", "\"");
            hashtag = hashtag.substring(1, hashtag.length() - 1);
            content = Bytes.toString(r.getValue(bColFamily, bCol_content));
            create_time = Bytes.toString(r.getValue(bColFamily, bCol_createTime));
            tweet_id = Bytes.toString(r.getValue(bColFamily, bColTweet));

            if (Bytes.toString(r.getValue(bColFamily, bCol_user)).equals(userid)) {
                if (!Bytes.toString(r.getValue(bColFamily, bCol_reply)).equals("\"\"")) {
                    reply_to_id = Bytes.toString(r.getValue(bColFamily, bCol_reply));
                    addTweetReply(userid, content, reply_to_id, hashtag, create_time,tweet_id);
                    groupby(reply_to_id, "reply");
                }
                if (!Bytes.toString(r.getValue(bColFamily, bCol_retweet)).equals("\"\"")) {
                    retweet_to_id = Bytes.toString(r.getValue(bColFamily, bCol_retweet));
                    addTweetRetweet(userid, content, retweet_to_id, hashtag, create_time,tweet_id);
                    groupby(retweet_to_id, "retweet");
                }
            } else {
                if (Bytes.toString(r.getValue(bColFamily, bCol_reply)).equals(userid)) {
                    reply_to_id = Bytes.toString(r.getValue(bColFamily, bCol_user));
                    addTweetReply(userid, content, reply_to_id, hashtag, create_time,tweet_id);
                    groupby(reply_to_id, "reply");
                }
                if (Bytes.toString(r.getValue(bColFamily, bCol_retweet)).equals(userid)) {
                    retweet_to_id = Bytes.toString(r.getValue(bColFamily, bCol_user));
                    addTweetRetweet(userid, content, retweet_to_id, hashtag, create_time,tweet_id);
                    groupby(retweet_to_id, "retweet");
                }
            }

        }

        // Cleanup
        rs.close();
        countInteract();
        if (allMap.size() == 0)
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
    public void addTweetReply(String sender_uid, String content, String reply_to_uid, String hashtags, String create_time,String tweet_id) {
        SendTweet tweet = new SendTweet();
        tweet.setReply(sender_uid, content, reply_to_uid, hashtags, create_time,tweet_id);
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
    public void addTweetRetweet(String sender_uid, String content, String retweet_to_uid, String hashtags, String create_time,String tweet_id) {
        SendTweet tweet = new SendTweet();
        tweet.setRetweet(sender_uid, content, retweet_to_uid, hashtags, create_time,tweet_id);
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
     */
    public void countInteract() {
        for (String id : allMap.keySet()) {
            allMap.get(id).calculateInteractionScore();
        }
    }

    public void calHashTagScore(String userid) throws IOException, ParseException {
        HashMap<String, HashMap<String, Integer>> usersHashTag = new HashMap<>();
        //popular_hashtags = readPopularHashtags();
        //userList = userid + userList;
        //Set self-retweet and self-reply Score = 1

        if (allMap.containsKey(userid)) {// if exist self-retweet and self-reply
            allMap.get(userid).setSame_tag_count(1);
        }

        if (userList.length() != 0) {
            userList = userid + userList;
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
                hashTag = Bytes.toString(r.getValue(bColFamily, bColHash));
                hashTag = hashTag.replace("\\\"", "\"");
                if(hashTag.length()>2){
                hashTag = hashTag.substring(1, hashTag.length() - 1);}
                get_user_id = Bytes.toString(r.getValue(bColFamily, bColUser));
                screen_name = Bytes.toString(r.getValue(bColFamily, bColScreenName));
                description = Bytes.toString(r.getValue(bColFamily, bColDescription));
                if (get_user_id.equals(userid)) {//if get userid = user id
                    if (allMap.containsKey(userid)) {// check whether have reply-retweet
                        // if yes, update the user information
                        allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
                    }
                } else {
                    // if get userid!= user id, update information
                    allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
                }
                HashMap<String, Integer> userHashTagFilter = userHashTag(hashTag);
                usersHashTag.put(get_user_id, userHashTagFilter);
            }
            HashMap<String, Integer> requestUserHashTag = usersHashTag.get(userid);
            for (String hashtag : requestUserHashTag.keySet()) {
                Integer count = requestUserHashTag.get(hashtag);//find the count of request user_id
                for (String id : usersHashTag.keySet()) {
                    if (!id.equals(userid)) {
                        if (usersHashTag.get(id).containsKey(hashtag)) {// if contact user_id also has the hashtag
                            count = count + usersHashTag.get(id).get(hashtag);// add the count
                            allMap.get(id).setSame_tag_count(count);//store in user
                        }
                    }
                }
            }

            countHashTag();//count all hashtags
        }
    }

    /**
     * count final interaction score
     */
    public void countHashTag() {
        for (String id : allMap.keySet()) {
            allMap.get(id).calculateFinalHashTagScore();
        }
    }

    public static HashMap<String, Integer> userHashTag(String hashTags) throws IOException, ParseException {
        HashMap<String, Object> userHashTag = json2Map(hashTags);
        HashMap<String, Integer> userUpdate = new HashMap<>();
        //popular_hashtags = readPopularHashtags();
        for (String hashtag : userHashTag.keySet()) {
            if (!popular_hashtags.containsKey(hashtag)) {
                //if the hashtag is not in the popular_hashtags
                int count = (int) userHashTag.get(hashtag);
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


    public void calKeyScore(String phrase, String hashtag, String type) throws ParseException, IOException {
        if (type.equals("retweet")) {
            updateKWCountWithLatest(phrase, hashtag, tweetsRetweet);
        } else if (type.equals("reply")) {
            updateKWCountWithLatest(phrase, hashtag, tweetsReply);
        } else {
            updateKWCountWithLatest(phrase, hashtag, tweetsRetweet);
            updateKWCountWithLatest(phrase, hashtag, tweetsReply);
        }

        for (Map.Entry<String, UserRow> user : allMap.entrySet()) {
            UserRow userRow = user.getValue();
            userList += userRow.calculateHashtagScore();
        }
    }

    public void updateKWCountWithLatest(String phrase, String hashtag, ArrayList<SendTweet> tweetsReplyOrRetweet) throws ParseException, IOException {

        for (SendTweet tweet : tweetsReplyOrRetweet) {
            String userId = tweet.getReply_to_uid();
            UserRow currentUser = allMap.get(userId);

            if (currentUser != null) {
                String content = tweet.getContent();
                String hashTags = tweet.getHashtags();

                // add phrase match:
                int phrase_match = StringUtils.countMatches(content, phrase);
                currentUser.addPhraseMatch(phrase_match);

                // add hashtag match:
                HashMap<String, Object> hashtagMap = json2Map(hashTags);
                if (hashtagMap.containsKey(hashtag)) {
                    double hashtag_match = (double) hashtagMap.get(hashtag);
                    int hashtag_match_int = (int) hashtag_match;
                    currentUser.addHashtagMatch(hashtag_match_int);
                } else {
                    currentUser.addHashtagMatch(0);
                }

                //update lasted contact:
                currentUser.updateTweet(tweet.getContent(), tweet.getCreate_time(), tweet.getTweetId());
            }
        }
    }


    public String returnSortedUsers() {
        String httpReply = "NoeatNosleep2021,554415272511";
        //sort with tree map:

        TreeMap<UserRow, String> treeMap = new TreeMap<>(new CompareScoreId());

        for (Map.Entry<String, UserRow> user : allMap.entrySet()) {
            UserRow userRow = user.getValue();
            userRow.roundScore();
            String userId = user.getKey();
            treeMap.put(userRow, userId);
        }

        for (Map.Entry<UserRow, String> entry : treeMap.entrySet()) {
            if (entry.getKey().getScore() != 0.0) {
                httpReply += entry.getKey().toString();
            }
        }

        return httpReply;
    }

    public static HashMap<String, Object> json2Map(String str) throws IOException {
//        str = str.replace("\\\"", "\"");
//        str = str.substring(1,str.length()-1);
        HashMap<String, Object> response = new ObjectMapper().readValue(str, HashMap.class);
        return response;
    }

    public static void main(String[] args) throws IOException, ParseException {

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

//
        String hashTag = "{\"إدكآر\":1}";

        hashTag = hashTag.replace("\"", "");


//        System.out.println(hashTag.getBytes(StandardCharsets.UTF_8));
//        System.out.println(Bytes.toBytes(hashTag));
//        byte[] byteArrray= hashTag.getBytes(StandardCharsets.UTF_8);
//        for(int i = 0;i<byteArrray.length;i++){
//            char a = (char) byteArrray[i];
//            System.out.println(byteArrray[i]+":"+a);}
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, String> map = mapper.readValue(hashTag, Map.class);
//        System.out.println(map.toString());

//        for(char ch : hashTag.toCharArray()){
//            if(ch<0|ch>255)
//            System.out.format("\\u%04x", (int) ch);
//        else
//                System.out.println(ch);}
//        String hex = "\\u062f\\u0643\\u0622\\u0631";
//        String input = hex;
//        String reader = new String("{\\\"لزيادة_المتابعين\\\":1,\\\"ريتويت\\\":2,\\\"poets\\\":1}");
//        reader = reader.replace("\\\"", "\"");
//        System.out.println(reader);
//        String str = "{\"poets\":1}";
        String str = "{\\\"luhan\\\":1,\\\"鹿晗\\\":1,\\\"luhanwishseason\\\":1}";
        str = "{}".replace("\\\"", "\"");
        System.out.println(str);
        HashMap<String, Object> helllo = json2Map(str);
        System.out.println(json2Map(str).toString());



    }
}
