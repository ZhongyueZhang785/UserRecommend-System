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


public class Tweet2 {

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
    public Tweet2(Connection connection) throws IOException, SQLException {
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

//        System.out.println("---------------------------------------------begin-----------------------------------------------");
//        System.out.println(sql);
        //get user connection list according to user id
        ResultSet resultUser = stmt.executeQuery(sql);

        if (!resultUser.next()) {
            //no such user in the user table
            return false;
        }

        //update interact user information in all map, will also store the type that do not need
        //get connection user set
        Set connectionUserSet = getConnectUserSet(resultUser, type);
        if (connectionUserSet.size() == 0) {
            //no contact tweets in required type
            return false;
        }
        //store requestUserIdHashTag
        storeRequestUserIdHashTag(resultUser);
        //get all connection rowkey in a list
        ArrayList<String> connectionRowKey = getConnectRowKey(connectionUserSet, userid);
        //turn it into (1,2,3,4) format
        String connectionRowKeyStr = sqlList(connectionRowKey);
        //batch get contact tweet according to connectionRowKey
        sql = String.format("SELECT contactKey,interaction_score, reply_tweet_id_list, retweet_tweet_id_list " +
                "FROM contact " +
                "WHERE contactKey in %s", connectionRowKeyStr);
        //System.out.println(sql);
        ResultSet resultContact = stmt.executeQuery(sql);
        int flag = 0;//if there has results the flag will be 1
        while (resultContact.next()) {
            //get interaction score from contact table
            String interaction_score_str = resultContact.getString("interaction_score");
            String useridContact = getUserIdContact(resultContact, userid);
            double interaction_score = Double.parseDouble(interaction_score_str);
            String tweet_list = listTweetRequired(type, resultContact);
            //only required type tweet's interaction scores will not be zero
            getInteractionScoreAndStoreConnectTweet(tweet_list, userid, interaction_score, useridContact);
            flag = 1;
        }
        if (flag == 0) {
            //no contact tweet find
            resultUser.close();
            resultContact.close();
            return false;
        }
        resultUser.close();
        resultContact.close();
        return true;
    }

    private void storeRequestUserIdHashTag(ResultSet resultUser) throws SQLException, IOException, ParseException {
        String hash_tag = resultUser.getString("hash_tag");
        hash_tag = decompress(hash_tag);
        requestUsersHashTag = userHashTag(hash_tag);
    }

    private String getUserIdContact(ResultSet resultContact, String userid) throws SQLException {
        String contactKey = resultContact.getString("contactKey");
        String[] users = contactKey.split("~");
        String useridContact = null;
        if (users[0].equals(userid)) {
            useridContact = users[1];
        } else {
            useridContact = users[0];
        }
        return useridContact;
    }

    private void getInteractionScoreAndStoreConnectTweet(String tweet_list, String userid, double interaction_score, String useridContact) throws JSONException {
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
                    //UserRow user = new UserRow(useridContact, interaction_score);
                    if (allMap.containsKey(useridContact)) {
                        UserRow user = allMap.get(useridContact);
                        user.setScore(useridContact, interaction_score);
                        allMap.put(useridContact, user);
                    }
                }
            }
        }

    }

    private String listTweetRequired(String type, ResultSet resultContact) throws SQLException {
        //store the tweet that has required type
        String tweet_list = null;
        if (type.equals("reply")) {
            tweet_list = resultContact.getString("reply_tweet_id_list");
            tweet_list = decompress(tweet_list);
            //tweet_list = dbListFormat(tweet_list);
        } else if (type.equals("retweet")) {
            tweet_list = resultContact.getString("retweet_tweet_id_list");
            tweet_list = decompress(tweet_list);
            //tweet_list = dbListFormat(tweet_list);
        } else if (type.equals("both")) {
            String reply_tweet_list = resultContact.getString("reply_tweet_id_list");
            reply_tweet_list = decompress(reply_tweet_list);
//            if (reply_tweet_list.length() > 2) {
//                reply_tweet_list = reply_tweet_list.substring(1, reply_tweet_list.length() - 1);
//                //reply_tweet_list = dbListFormat(reply_tweet_list);
//            }
            String retweet_tweet_list = resultContact.getString("retweet_tweet_id_list");
            retweet_tweet_list = decompress(retweet_tweet_list);
//            if (retweet_tweet_list.length() > 2) {
//                retweet_tweet_list = retweet_tweet_list.substring(1, retweet_tweet_list.length() - 1);
//                //retweet_tweet_list = dbListFormat(retweet_tweet_list);
//            }
            tweet_list = reply_tweet_list.substring(0, reply_tweet_list.length() - 1) + "," +
                    retweet_tweet_list.substring(1);
        }
        return tweet_list;

    }


    private ArrayList<String> getConnectRowKey(Set connectionUserSet, String userid) {
        ArrayList<String> userConnectionList = new ArrayList<>();
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
                rowKeyContact = "\"" + rowKeyContact + "\"";
                userConnectionList.add(rowKeyContact);
            }

        }
        return userConnectionList;
    }


    public String dbListFormat(String str) {
        str = str.substring(1, str.length() - 1);
        str = str.replace("\\\"", "\"");
        str = str.replace("\\\\", "\\");
        return str;
    }

    public void updateUserInforamtion(String str) throws JSONException {
        //update user information according to reply/retweet list
        if (!str.equals("")) {
            JSONArray jsonArr = new JSONArray(str);
            for (int i = 0; i < jsonArr.length(); i++) {
                if (!jsonArr.isNull(i)) {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    String user_id = jsonObj.getString("user_id").toString();
                    String hashTag = jsonObj.getString("hash_tag").toString();
                    //hashTag = decompress(hashTag);
                    String description = jsonObj.getString("description").toString();
                    //description = decompress(description);
                    String screen_name = jsonObj.getString("screen_name").toString();
                    //screen_name = decompress(screen_name);
                    //put information user hashTag, description, screen_name into all map
                    UserRow user = new UserRow(hashTag, description, screen_name);
                    allMap.put(user_id, user);
                }
            }
        }

    }

    public Set getConnectUserSet(ResultSet resultUser, String type) throws SQLException, JSONException {
        if (type.equals("reply")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            updateUserInforamtion(reply_to_user_id_list);//put user information into all map according to reply
        } else if (type.equals("retweet")) {
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(retweet_to_user_id_list);//put user information into all map according to retweet
        } else if (type.equals("both")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(reply_to_user_id_list);//put user information into all map according to reply
            updateUserInforamtion(retweet_to_user_id_list);//put user information into all map according to retweet
        }

        Set connectionUserSet = allMap.keySet();
        return connectionUserSet;
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

    public String sqlList(ArrayList<String> itemList) {
        ArrayList<String> rowkeyList = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            rowkeyList.add(itemList.get(i));
        }
        String list = rowkeyList.toString();
        list = list.replace("[", "(");
        list = list.replace("]", ")");
        return list;
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


}
