package twitter_utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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

    private static Statement stmt;

    //Store all result: key user_id/score/screen/description/text/hashTag
    HashMap<String, UserRow> allMap = new HashMap<String, UserRow>();
    //store the connection tweets with right type
    ArrayList<SendTweet> tweetsContact = new ArrayList<>();
    //store the user whose score is not zero
    ArrayList<String> userList = new ArrayList<>();
    //store request user's hashTag
    HashMap<String, Integer> requestUsersHashTag = new HashMap<>();

    //constructor:
    public Tweet(Connection connection) throws IOException, SQLException {
        stmt = connection.createStatement();
    }

    public static String decompress(String compressedJson) {
        /*
        Base64 decoder and Zlib decompress
         */
        org.apache.commons.codec.binary.Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(compressedJson.getBytes()));

        return decodedString;
    }

    /**
     * Calculate total interaction score
     *
     * @param userid
     * @return
     * @throws IOException
     */
    public Boolean calInteractionScore(String userid, String type) throws JSONException, SQLException, IOException, ParseException {

        String sql = null;
        if (type.equals("both")) {// only get the required type user_id
            sql = String.format("SELECT hash_tag,retweet_to_user_id_list, reply_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userid);
        } else if (type.equals("reply")) {
            sql = String.format("SELECT hash_tag,reply_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userid);
        } else if (type.equals("retweet")) {
            sql = String.format("SELECT hash_tag,retweet_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userid);
        }

        //System.out.println("---------------------------------------------begin-----------------------------------------------");
        //System.out.println(sql);
        //get user connection list according to user id
        ResultSet resultUser = stmt.executeQuery(sql);

        if (!resultUser.next()) {
            //no such user in the user table
            return false;
        }

        //update interact user information in all map with user_id,hash_tag,description,screen_name,interaction_score
       getConnectUserSet(resultUser, type);

        //store requestUserIdHashTag
        storeRequestUserIdHashTag(resultUser);
        resultUser.close();
        return true;
    }

    private void storeRequestUserIdHashTag(ResultSet resultUser) throws SQLException, IOException, ParseException {
        String hash_tag = resultUser.getString("hash_tag");
        hash_tag = decompress(hash_tag);
        requestUsersHashTag = userHashTag(hash_tag);
    }


    private void StoreConnectTweet(String tweet_list,String user_id) throws JSONException {
        //format
        tweet_list = tweet_list.replace("\\\"", "\"");
        tweet_list = tweet_list.replace("\"{\"", "{\"");
        tweet_list = tweet_list.replace("\"}\"","\"}" );
        tweet_list = tweet_list.replace("\\\\\"","\\\"");

        if (!tweet_list.equals("")) {
            JSONArray jsonArr = new JSONArray(tweet_list);
            for (int i = 0; i < jsonArr.length(); i++) {
                if (!jsonArr.isNull(i)) {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    SendTweet sendtweet = new SendTweet(user_id,
                            jsonObj.get("create_time").toString(),
                            jsonObj.get("hash_tag").toString(),
                            jsonObj.get("tweet_id").toString(),
                            jsonObj.get("text").toString());
                    tweetsContact.add(sendtweet);
                }
            }
        }

    }



    public void updateUserInforamtion(String str,String type) throws JSONException {
        //update user information according to reply/retweet list
        String listName = null;
        if(type.equals("reply")){
            listName ="reply_tweet_list";
        }else if(type.equals("retweet")){
            listName = "retweet_tweet_list";
        }

        if (!str.equals("")) {
            JSONArray jsonArr = new JSONArray(str);
            for (int i = 0; i < jsonArr.length(); i++) {
                if (!jsonArr.isNull(i)) {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    String user_id = jsonObj.getString("user_id").toString();
                    String hashTag = jsonObj.getString("hash_tag").toString();
                    String description = jsonObj.getString("description").toString();
                    String screen_name = jsonObj.getString("screen_name").toString();
                    String interaction_score_str = jsonObj.getString("interaction_score");
                    double interaction_score = Double.parseDouble(interaction_score_str);
                    //put information user hashTag, description, screen_name into all map
                    UserRow user = new UserRow(user_id,hashTag, description, screen_name,interaction_score);
                    allMap.put(user_id, user);
                    //store the connect tweet into tweetsConatct
                    String tweet_list = jsonObj.getString(listName);
                    StoreConnectTweet(tweet_list,user_id);
                }
            }
        }

    }

    public void getConnectUserSet(ResultSet resultUser, String type) throws SQLException, JSONException {
        if (type.equals("reply")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            updateUserInforamtion(reply_to_user_id_list,"reply");//put user information into all map according to reply
        } else if (type.equals("retweet")) {
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(retweet_to_user_id_list,"retweet");//put user information into all map according to retweet
        } else if (type.equals("both")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(reply_to_user_id_list,"reply");//put user information into all map according to reply
            updateUserInforamtion(retweet_to_user_id_list,"retweet");//put user information into all map according to retweet
        }
    }

    public void calHashTagScore(String userid) throws IOException, ParseException, SQLException {

        //popular_hashtags = readPopularHashtags();
        //Set self-retweet and self-reply Score = 1

        if (userList.size() != 0) {//still has non-zero interaction user maybe include request user himself
            for (String user_id : userList) {
                if (!user_id.equals(userid)) {//if self-reply and retweet set hashtag score to 1,same tag account = 0
                    String hashTag = allMap.get(user_id).getHashTag();
                    HashMap<String, Integer> userHashTag = userHashTag(hashTag);
                    for (String hashtag : requestUsersHashTag.keySet()) {//find the same hashTag and accumulate the account
                        Integer count = requestUsersHashTag.get(hashtag);//find the count of request user_id
                        if (userHashTag.containsKey(hashtag)) {
                            int newCount = count + userHashTag.get(hashtag);// add the count
                            allMap.get(user_id).setSame_tag_count(newCount);//store in user
                        }
                    }
                }
            }
            countHashTag();//count all hashtags
        }

        //System.out.println("-------------------------------------end--------------------------------------------");
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
            if(userRow.calculateHashtagScore()!=null){
            userList.add(userRow.calculateHashtagScore());}
        }
    }

    public void updateKWCountWithLatest(String phrase, String hashtag, ArrayList<SendTweet> tweetsReplyOrRetweet) throws
            ParseException, IOException {

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

    public static void main(String[] args) throws JSONException {

        String b = "[{\"create_time\":\"2014-03-29 22:54:31\",\"text\":\"Love not wearing makeup #fresh\",\"tweet_id\":\"449922558092378112\",\"hash_tag\":\"{\\\"fresh\\\":1}\"}," +
                "{\"create_time\":\"2014-04-10 21:44:59\",\"text\":\"RT @h_4_2: ⭕#آسرع_رتويت ➊ ريتويت ➋ فولومي @h_4_2 ➌ اضافة من عمل ريتويت ➍ اشترك بالريتويت التلقائي http://t.co/07xxAptQSW \\n7563\",\"tweet_id\":\"454253713855881217\",\"hash_tag\":\"{\\\"آسرع_رتويت\\\":1}\"}]";
        String  tweet_list = b;
        //System.out.println(tweet_list);
        tweet_list = "[\"{\\\"tweet_id\\\":\\\"454922822776004609\\\",\\\"text\\\":\\\"RT @h_4_6: ⭕#آسرع_رتويت ➊ ريتويت ➋ فولومي @h_4_6 ➌ اضافة من عمل ريتويت ➍ اشترك بالريتويت التلقائي http:\\/\\/t.co\\/a0Koxg7FPR \\\\n7486\\\",\\\"hash_tag\\\":\\\"{\\\\\\\"آسرع_رتويت\\\\\\\":1}\\\",\\\"create_time\\\":\\\"2014-04-12 10:03:47.0\\\"}\"]";
        tweet_list = tweet_list.replace("\\\"", "\"");
        tweet_list = tweet_list.replace("\"{\"", "{\"");
        tweet_list = tweet_list.replace("\"}\"","\"}" );
        tweet_list = tweet_list.replace("\\\\\"","\\\"");

        //System.out.println(tweet_list);
        JSONArray jsonArr = new JSONArray(tweet_list);
//        JSONObject jsonObj = jsonArr.getJSONObject(0);


//        String b = "[{\"create_time\":\"2014-03-29 22:54:31\",\"text\":\"Love not wearing makeup #fresh\",\"tweet_id\":\"449922558092378112\",\"hash_tag\":\"{\\\"fresh\\\":1}\"}," +
//                "{\"create_time\":\"2014-04-10 21:44:59\",\"text\":\"RT @h_4_2: ⭕#آسرع_رتويت ➊ ريتويت ➋ فولومي @h_4_2 ➌ اضافة من عمل ريتويت ➍ اشترك بالريتويت التلقائي http://t.co/07xxAptQSW \\n7563\",\"tweet_id\":\"454253713855881217\",\"hash_tag\":\"{\\\"آسرع_رتويت\\\":1}\"}]";
//        tweet_list = b;
//        System.out.println(tweet_list);
//        jsonArr = new JSONArray(tweet_list);
//
//        for (int i = 0; i < jsonArr.length(); i++) {
//            if (!jsonArr.isNull(i)) {
//                JSONObject jsonObj = jsonArr.getJSONObject(i);
//                SendTweet sendtweet = new SendTweet("123",
//                        jsonObj.get("create_time").toString(),
//                        jsonObj.get("hash_tag").toString(),
//                        jsonObj.get("tweet_id").toString(),
//                        jsonObj.get("text").toString());
//                System.out.println(sendtweet);
//            }
//        }

    }


}
