package utility;

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

    String userIdRequest;
    String typeRequest;
    String phaseRequest;
    String hashTagRequest;

    //constructor:
    public Tweet2(Connection connection, String userIdRequest, String typeRequest, String phaseRequest, String hashTagRequest) throws IOException, SQLException {
        stmt = connection.createStatement();
        this.userIdRequest = userIdRequest;
        this.typeRequest = typeRequest;
        this.phaseRequest = phaseRequest;
        this.hashTagRequest = hashTagRequest;
    }

    public static String decompress(String compressedJson) {
        /*
        Base64 decoder and Zlib decompress
         */
        org.apache.commons.codec.binary.Base64 base64 = new Base64();
        String decodedString = new String(base64.decode(compressedJson.getBytes()));

        return decodedString;
    }


    public Boolean calInteractionScore() throws JSONException, SQLException, IOException, ParseException {

        String sql = null;
        if (typeRequest.equals("both")) {// only get the required type user_id
            sql = String.format("SELECT hash_tag,retweet_to_user_id_list, reply_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userIdRequest);
        } else if (typeRequest.equals("reply")) {
            sql = String.format("SELECT hash_tag,reply_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userIdRequest);
        } else if (typeRequest.equals("retweet")) {
            sql = String.format("SELECT hash_tag,retweet_to_user_id_list" +
                    " FROM user WHERE user_id = %s", userIdRequest);
        }

//        System.out.println("---------------------------------------------begin-----------------------------------------------");
//        System.out.println(sql);
        //get user connection list according to user id
        ResultSet resultUser = stmt.executeQuery(sql);

        if (!resultUser.next()) {
            //no such user in the user table
            return false;
        }
        //store requestUserIdHashTag
        storeRequestUserIdHashTag(resultUser);

        //update interact user information in all map with user_id,hash_tag,description,screen_name,interaction_score
        getConnectUserSet(resultUser, typeRequest);
        resultUser.close();
        return true;
    }

    private void storeRequestUserIdHashTag(ResultSet resultUser) throws SQLException, IOException, ParseException {
        String hash_tag = resultUser.getString("hash_tag");
        hash_tag = decompress(hash_tag);
        requestUsersHashTag = userHashTag(hash_tag);
    }




    public void updateUserInforamtion(String str, String type) throws JSONException, IOException, ParseException {
        //update user information according to reply/retweet list
        String listName = null;
        if (type.equals("reply")) {
            listName = "reply_tweet_list";
        } else if (type.equals("retweet")) {
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
                    //store tweet list match hashTag and match phrase
                    String tweet_list = jsonObj.getString(listName);
                    UserRow user = new UserRow();
                    if (!allMap.containsKey(user_id)) {
                        //no such user before
                        //put information user hashTag, description, screen_name into all map
                        //set interaction score
                        user.setUserRow(user_id, hashTag, description, screen_name, interaction_score);
                    } else {
                        //if already in the map, get from map
                        user = allMap.get(user_id);
                    }
                    //update phrase match and key match and update the newest tweet
                    user = KeyScoreMatch(tweet_list, user_id, user);
                    //calculate key score
                    if (user.calculateKeyScore()) {
                        //if contain such required type
                        //add hashtag
                        user = HashTagScoreMatch(hashTag, user);
                        //calculate hashtag score
                        user.calculateFinalHashTagScore();
                        //put user into all map
                        allMap.put(user_id, user);
                    }


                }
            }
        }

    }

    private UserRow HashTagScoreMatch(String hashTag, UserRow user) throws IOException, ParseException {
        HashMap<String, Integer> userHashTag = userHashTag(hashTag);
        for (String hashtag : requestUsersHashTag.keySet()) {//find the same hashTag and accumulate the account
            Integer count = requestUsersHashTag.get(hashtag);//find the count of request user_id
            if (userHashTag.containsKey(hashtag)) {
                int newCount = count + userHashTag.get(hashtag);// add the count
                user.setSame_tag_count(newCount);//store in user
            }
        }

        return user;
    }

    private UserRow KeyScoreMatch(String tweet_list, String user_id, UserRow user) throws JSONException, IOException {
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
                    //add phrase match
                    user.addPhraseMatch(sendtweet.phraseMatch(phaseRequest));//add phrase match
                    //add hashtag match
                    user.addHashtagMatch(sendtweet.hashTagMatch(hashTagRequest));
                    //update to newest tweet content
                    user.updateTweet(sendtweet.getContent(), sendtweet.getCreate_time(), sendtweet.getTweetId());
                }
            }
            return user;
        }
        return null;
    }

    public void getConnectUserSet(ResultSet resultUser, String type) throws SQLException, JSONException, IOException, ParseException {
        if (type.equals("reply")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            updateUserInforamtion(reply_to_user_id_list, "reply");//put user information into all map according to reply
        } else if (type.equals("retweet")) {
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(retweet_to_user_id_list, "retweet");//put user information into all map according to retweet
        } else if (type.equals("both")) {
            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
            reply_to_user_id_list = decompress(reply_to_user_id_list);
            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
            retweet_to_user_id_list = decompress(retweet_to_user_id_list);
            updateUserInforamtion(retweet_to_user_id_list, "retweet");///put user information into all map according to reply
            updateUserInforamtion(reply_to_user_id_list, "reply");
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
