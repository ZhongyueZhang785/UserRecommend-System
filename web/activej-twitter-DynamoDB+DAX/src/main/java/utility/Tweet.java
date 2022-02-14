package utility;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Condition;

// sorting:

// Descending order
class CompareScoreId implements Comparator<UserRow> {
    public int compare(UserRow u1, UserRow u2) {
        if (Double.compare(u2.getScore(), u1.getScore()) != 0) {
            return Double.compare(u2.getScore(), u1.getScore());
        } else {
            if ((Long.valueOf(u2.getUser_id()) - Long.valueOf(u1.getUser_id())) < 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}


public class Tweet {
    private static DynamoDB dynamoDB;
    /**
     * Table name
     */
    private static String tableNameUser = "user";
    /**
     * Table name
     */
    private static String tableNameContact = "contact";
    /**
     * Table handler.
     */
    private static Table bizTableuser;
    /**
     * Table handler.
     */
    private static Table bizTablecontact;
    //Store all result: key user_id/score/screen/description/text
    HashMap<String, UserRow> allMap = new HashMap<String, UserRow>();
    //store the connection tweets with right type
    ArrayList<SendTweet> tweetsContact = new ArrayList<>();
    //store the user whose score is not zero
    ArrayList<String> userList = new ArrayList<>();

    //constructor:
    public Tweet(DynamoDB dynamoDB) throws IOException {
        bizTableuser = dynamoDB.getTable(tableNameUser);
        bizTablecontact = dynamoDB.getTable(tableNameContact);
        this.dynamoDB = dynamoDB;
    }

    /**
     * Calculate total interaction score
     *
     * @param userid
     * @return
     * @throws IOException
     */
    public Boolean calInteractionScore(String userid, String type) throws IOException, JSONException {
        //reverse the user id to get rowkey
        StringBuilder b = new StringBuilder(userid);
        String reverseUserId = b.reverse().toString();

        //get information from user
        Item item = bizTableuser.getItem("userKey", reverseUserId);

        if (item != null) {
            String reply_to_user_id_list = (String) item.get("reply_to_user_id_list");
            String retweet_to_user_id_list = (String) item.get("retweet_to_user_id_list");
            String connection_user_id_list = reply_to_user_id_list.substring(1, reply_to_user_id_list.length() - 1) + ","
                    + retweet_to_user_id_list.substring(1, retweet_to_user_id_list.length() - 1);
            List connectionUserList = Arrays.asList(connection_user_id_list.split(","));
            Set connectionUserSet = new HashSet(connectionUserList);//maybe exist null value due to the collection
            if (connectionUserSet.size() == 0) {
                //no contact tweets in both retweet and reply
                return false;
            }
            for (Object objUserid : connectionUserSet) {//get interaction score in contact table
                if (!objUserid.equals("")) {
                    String useridContact = objUserid.toString();
                    String rowKeyContact = null;
                    if (useridContact.compareTo(userid) < 0) {
                        //useridContact<userid
                        rowKeyContact = useridContact + "~" + userid;
                    } else {
                        //useridContact>userid
                        rowKeyContact = userid + "~" + useridContact;
                    }

                    b = new StringBuilder(rowKeyContact);
                    String reverseRowKeyContact = b.reverse().toString();
                    //get information from contact table
                    item = bizTablecontact.getItem("contactKey", reverseRowKeyContact);
                    double interaction_score = item.getDouble("interaction_score");
//                    //put information into all map
//                    UserRow user = new UserRow(useridContact, interaction_score);
//                    allMap.put(useridContact, user);
                    String tweet_list = null;

                    //store the tweet that has required type
                    if (type.equals("reply")) {
                        tweet_list = item.getString("reply_tweet_id_list");
                        tweet_list = tweet_list.substring(1, tweet_list.length() - 1);
                        tweet_list = tweet_list.replace("\\\"", "\"");
                        tweet_list = tweet_list.replace("\\\\", "\\");
                    } else if (type.equals("retweet")) {
                        tweet_list = item.getString("retweet_tweet_id_list");
                        tweet_list = tweet_list.substring(1, tweet_list.length() - 1);
                        tweet_list = tweet_list.replace("\\\"", "\"");
                        tweet_list = tweet_list.replace("\\\\", "\\");

                    } else if (type.equals("both")) {
                        String reply_tweet_list = item.getString("reply_tweet_id_list");
                        if (reply_tweet_list.length() > 2) {
                            reply_tweet_list = reply_tweet_list.substring(1, reply_tweet_list.length() - 1);
                            reply_tweet_list = reply_tweet_list.replace("\\\"", "\"");
                            reply_tweet_list = reply_tweet_list.replace("\\\\", "\\");
                        }
                        String retweet_tweet_list = item.getString("retweet_tweet_id_list");
                        if (retweet_tweet_list.length() > 2) {
                            retweet_tweet_list = retweet_tweet_list.substring(1, retweet_tweet_list.length() - 1);
                            retweet_tweet_list = retweet_tweet_list.replace("\\\"", "\"");
                            retweet_tweet_list = retweet_tweet_list.replace("\\\\", "\\");
                        }
                        tweet_list = reply_tweet_list.substring(0, reply_tweet_list.length() - 1) + "," +
                                retweet_tweet_list.substring(1);

                    }

                    if (!tweet_list.equals("")) {
                        JSONArray jsonArr = new JSONArray(tweet_list);
                        for (int i = 0; i < jsonArr.length(); i++) {
                            if (!jsonArr.isNull(i)) {
                                JSONObject jsonObj = jsonArr.getJSONObject(i);
                                SendTweet sendtweet = new SendTweet(userid, useridContact,
                                        jsonObj.get("create_time").toString(),
                                        jsonObj.get("hash_tag").toString(),
                                        jsonObj.get("tweet_id").toString(),
                                        jsonObj.get("text").toString());
                                tweetsContact.add(sendtweet);
                                //put information into all map
                                //interaction score is not null and key score is not null
                                UserRow user = new UserRow(useridContact, interaction_score);
                                allMap.put(useridContact, user);
                            }
                        }
                    }


                }
            }

            return true;

        }

        return false;


    }


    public void calHashTagScore(String userid) throws IOException, ParseException {
        HashMap<String, HashMap<String, Integer>> usersHashTag = new HashMap<>();
        //popular_hashtags = readPopularHashtags();
        //Set self-retweet and self-reply Score = 1
        TableKeysAndAttributes contactTableKeysAndAttributes = new TableKeysAndAttributes(tableNameUser);


        if (userList.size() != 0) {//still has non-zero interaction user maybe include request user himself
            if (allMap.containsKey(userid)) {// if exist self-retweet and self-reply
                //no need to add user id again into queryRowList
                allMap.get(userid).setSame_tag_count(1);
            } else {//add into userList, so that can get request user hashtag
                userList.add(userid);
            }
            if (userList.size() != 1 | (userList.size() == 1 && allMap.containsKey(userid))) {//except request user there are other user has non-zero score
                // Add a partition key
                List<String> keys = new ArrayList<>();
                for (String connectUserId : userList) {
                    //reverse the user id to get rowkey
                    if (!connectUserId.equals(null)) {
                        StringBuilder b = new StringBuilder(connectUserId);
                        String reverseUserId = b.reverse().toString();
                        keys.add(reverseUserId);
                    }
                }
                contactTableKeysAndAttributes.addHashOnlyPrimaryKeys("userKey", keys.toArray(new String[keys.size()]));
                BatchGetItemOutcome outcome = dynamoDB.batchGetItem(contactTableKeysAndAttributes);
                Map<String, KeysAndAttributes> unprocessed = null;

                String hashTag = null;
                String get_user_id = null;
                String screen_name = null;
                String description = null;
                int n = 0;
                do {
                    for (String tableName : outcome.getTableItems().keySet()) {
                        System.out.println("Items in table " + tableName);
                        List<Item> items = outcome.getTableItems().get(tableName);
                        for (Item item : items) {
                            if (item != null) {
                                hashTag = (String) item.get("hash_tag");
                                hashTag = hashTag.replace("\\\"", "\"");
                                if (hashTag.length() > 2) {
                                    hashTag = hashTag.substring(1, hashTag.length() - 1);
                                }
                                get_user_id = (String) item.get("user_id");
                                screen_name = (String) item.get("screen_name");
                                description = (String) item.get("description");
                            } else {// if not contain user information in user table
                                hashTag = "";
                                screen_name = "";
                                description = "";
                                get_user_id = userList.get(n);

                            }
                            if (get_user_id.equals(userid)) {//if get userid = user id
                                // check whether have reply-retweet
                                // if yes, update the user information
                                if (allMap.containsKey(userid)) {
                                    allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
                                }
                            } else {
                                // if get userid!= user id, update information
                                allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
                            }
                            if (!hashTag.equals("")) {
                                HashMap<String, Integer> userHashTagFilter = userHashTag(hashTag);
                                usersHashTag.put(get_user_id, userHashTagFilter);
                            }
                            n = n + 1;
                        }
                    }
                    // Check for unprocessed keys which could happen if you exceed
                    // provisioned
                    // throughput or reach the limit on response size.
                    unprocessed = outcome.getUnprocessedKeys();

                    if (unprocessed.isEmpty()) {
                        System.out.println("No unprocessed keys found");
                    } else {
                        System.out.println("Retrieving the unprocessed keys");
                        outcome = dynamoDB.batchGetItemUnprocessed(unprocessed);
                    }
                } while (!unprocessed.isEmpty());
                accumuHashTag(userid,usersHashTag);
                countHashTag();//count all hashtags
            }
        }

    }

    public void accumuHashTag(String userid,HashMap<String, HashMap<String, Integer>>usersHashTag){
        HashMap<String, Integer> requestUserHashTag = usersHashTag.get(userid);
        for (String hashtag : requestUserHashTag.keySet()) {
            Integer count = requestUserHashTag.get(hashtag);//find the count of request user_id
            for (String id : usersHashTag.keySet()) {
                if (!id.equals(userid)) {
                    if (usersHashTag.get(id).containsKey(hashtag)) {// if contact user_id also has the hashtag
                        int newCount = count + usersHashTag.get(id).get(hashtag);// add the count
                        allMap.get(id).setSame_tag_count(newCount);//store in user
                    }
                }
            }
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
            //if the hashtag is not in the popular_hashtags
            int count = (int) userHashTag.get(hashtag);
            userUpdate.put(hashtag, count);

        }
        return userUpdate;

    }


    public void calKeyScore(String phrase, String hashtag) throws ParseException, IOException {
        updateKWCountWithLatest(phrase, hashtag, tweetsContact);
        for (Map.Entry<String, UserRow> user : allMap.entrySet()) {
            UserRow userRow = user.getValue();
            userList.add(userRow.calculateHashtagScore());
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
                hashtag = hashtag.toLowerCase();
                if (hashtagMap.containsKey(hashtag)) {
                    int hashtag_match_int = (int) hashtagMap.get(hashtag);
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
        HashMap<String, Object> response = new ObjectMapper().readValue(str, HashMap.class);
        return response;
    }

    public static void main(String[] args) throws IOException, ParseException, JSONException {

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
//        String reply_to_user_id_list = "[]";
//        String retweet_to_user_id_list = "[12,4]";
//        String connection_user_id_list = reply_to_user_id_list.substring(1, reply_to_user_id_list.length() - 1) + "," + retweet_to_user_id_list.substring(1, retweet_to_user_id_list.length() - 1);
//        List connectionList = Arrays.asList(connection_user_id_list.split(","));
//
//        Set connectionSet = new HashSet(connectionList);
//        System.out.println(connectionSet.toString());
//        String userid = "31";
//        for (Object objUserid : connectionSet) {//get interaction score in contact table
//            if (!objUserid.equals("")) {
//                String useridContact = objUserid.toString();
//                String rowKeyContact = null;
//                if (useridContact.compareTo(userid) < 0) {
//                    //useridContact<userid
//                    rowKeyContact = useridContact + "|" + userid;
//                } else {
//                    //useridContact>userid
//                    rowKeyContact = userid + "|" + useridContact;
//                }
//                StringBuilder b = new StringBuilder(rowKeyContact);
//                String reverseRowKeyContact = b.reverse().toString();
//                System.out.println(reverseRowKeyContact);
//
//            }
//
//        }
        //test extract information from contact table
//        String a = "[,{\"tweet_id\":\"460647120798613504\",\"text\":\"RT @AlArab_Qatar: #مصر|  أيمن نور رئيس حزب غد الثورة يعلن تدشين حملة جديدة تحمل اسم \\\"مقاطعون\\\" لمقاطعة الانتخابات الرئاسية المقبلة. #العرب\",\"hash_tag\":\"{\\\"العرب\\\":1,\\\"مصر\\\":1}\",\"create_time\":\"2014-04-28 05:10:06.0\"}]";
//        //String c = "[{\"ti\":\"447348723451183105\", \"ct\":\"2014-03-22 22:57:01.0\", \"tx\":\"RT @adorablexziall: idc if you're a in this fandom or not \\\\n\\\\nyou rt this bc Liam James Payne made it\\\\n\\\\n#KCA #VoteUKDirectioners http://t.co/j…\", \"ht\":\"{\"kca\":1,\"voteukdirectioners\":1}\"}]";
//        String c = "[,{\"ti\":\"447348723451183105\", \"ct\":\"2014-03-22 22:57:01.0\", \"tx\":\"RT @adorablexziall: idc if you're a in this fandom or not \\\\n\\\\nyou rt this bc Liam James Payne made it\\\\n\\\\n#KCA #VoteUKDirectioners http://t.co/j…\", \"ht\":\"{\\\\\"kca\\\\\":1,\\\\\"voteukdirectioners\\\\\":1}\"}]";
//        System.out.println(c);
//        c = c.replace("\\\\", "\\");
//        System.out.println(c);
//        String b = "[{\"create_time\":\"2014-03-29 22:54:31\",\"text\":\"Love not wearing makeup #fresh\",\"tweet_id\":\"449922558092378112\",\"hash_tag\":\"{\\\"fresh\\\":1}\"}," +
//                "{\"create_time\":\"2014-04-10 21:44:59\",\"text\":\"RT @h_4_2: ⭕#آسرع_رتويت ➊ ريتويت ➋ فولومي @h_4_2 ➌ اضافة من عمل ريتويت ➍ اشترك بالريتويت التلقائي http://t.co/07xxAptQSW \\n7563\",\"tweet_id\":\"454253713855881217\",\"hash_tag\":\"{\\\"آسرع_رتويت\\\":1}\"}]";
//        String reply_tweet_list = c;
//        String retweet_tweet_list = b;
//        String tweet_list = reply_tweet_list.substring(0, reply_tweet_list.length() - 1) + "," +
//                retweet_tweet_list.substring(1);
//        System.out.println(tweet_list);
//        JSONArray jsonArr = new JSONArray(tweet_list);
//
//        for (int i = 0; i < jsonArr.length(); i++) {
//            if (!jsonArr.isNull(i)) {
//                JSONObject jsonObj = jsonArr.getJSONObject(i);
//                SendTweet sendtweet = new SendTweet("123", "456",
//                        jsonObj.get("create_time").toString(),
//                        jsonObj.get("hash_tag").toString(),
//                        jsonObj.get("tweet_id").toString(),
//                        jsonObj.get("text").toString());
//                System.out.println(sendtweet);
//            }
//        }
        StringBuilder b = new StringBuilder("430845087");
        System.out.println(b.reverse().toString());
        b = new StringBuilder("430845087");
        System.out.println(b.reverse().toString());
        b = new StringBuilder("1699533926");
        System.out.println(b.reverse().toString());
        b = new StringBuilder("743292229");
        System.out.println(b.reverse().toString());

    }
}
