//package utility;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jettison.json.JSONArray;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//import java.sql.Statement;
//import java.io.IOException;
//import java.text.ParseException;
//import java.util.*;
//
//// sorting:
//
//// Descending order
//class CompareScoreId implements Comparator<UserRow> {
//    public int compare(UserRow u1, UserRow u2) {
//        if (Double.compare(u2.getScore(), u1.getScore()) != 0) {
//            return Double.compare(u2.getScore(), u1.getScore());
//        } else {
//            if ((Long.valueOf(u2.getUser_id()) - Long.valueOf(u1.getUser_id())) < 0) {
//                return -1;
//            } else {
//                return 1;
//            }
//        }
//    }
//}
//
//
//public class Tweet {
//
//    private static Statement stmt;
//
//    //Store all result: key user_id/score/screen/description/text
//    HashMap<String, UserRow> allMap = new HashMap<String, UserRow>();
//    //store the connection tweets with right type
//    ArrayList<SendTweet> tweetsContact = new ArrayList<>();
//    //store the user whose score is not zero
//    ArrayList<String> userList = new ArrayList<>();
//
//    //constructor:
//
//
//    public Tweet(Connection connection) throws IOException, SQLException {
//        stmt = connection.createStatement();
//    }
//
//    /**
//     * Calculate total interaction score
//     *
//     * @param userid
//     * @return
//     * @throws IOException
//     */
//    public Boolean calInteractionScore(String userid, String type) throws JSONException, SQLException {
//        //reverse the user id to get rowkey
//        //StringBuilder b = new StringBuilder(userid);
//        String reverseUserId = userid;
//        String sql = String.format("SELECT retweet_to_user_id_list, reply_to_user_id_list" +
//                " FROM user WHERE user_key = %s", reverseUserId);
//        System.out.println("begin");
//        System.out.println(sql);
//
//        //get user connection list according to user id
//        ResultSet resultUser = stmt.executeQuery(sql);
//
//        while (resultUser.next()) {
//            String reply_to_user_id_list = resultUser.getString("reply_to_user_id_list");
//            String retweet_to_user_id_list = resultUser.getString("retweet_to_user_id_list");
//            String connection_user_id_list = reply_to_user_id_list.substring(1, reply_to_user_id_list.length() - 1) + ","
//                    + retweet_to_user_id_list.substring(1, retweet_to_user_id_list.length() - 1);
//            List connectionUserList = Arrays.asList(connection_user_id_list.split(","));
//            Set connectionUserSet = new HashSet(connectionUserList);//maybe exist null value due to the collection
//            if (connectionUserSet.size() == 0) {
//                //no contact tweets in both retweet and reply
//                return false;
//            }
//            for (Object objUserid : connectionUserSet) {//get interaction score in contact table
//                if (!objUserid.equals("")) {
//                    String useridContact = objUserid.toString();
//                    String rowKeyContact = null;
//                    if (useridContact.compareTo(userid) < 0) {
//                        //useridContact<userid
//                        rowKeyContact = useridContact + "~" + userid;
//                    } else {
//                        //useridContact>userid
//                        rowKeyContact = userid + "~" + useridContact;
//                    }
//                    //b = new StringBuilder(rowKeyContact);
//                    String reverseRowKeyContact = rowKeyContact;
//                    reverseRowKeyContact = "\""+reverseRowKeyContact+"\"";
//                    //get information from contact table
//                    sql = String.format("SELECT interaction_score, reply_tweet_id_list, retweet_tweet_id_list FROM contact WHERE contactKey = %s", reverseRowKeyContact);
//                    System.out.println(sql);
//                    ResultSet resultContact = stmt.executeQuery(sql);
//                    if (!resultContact.next()) {
//                        resultContact.close();
//                        resultUser.close();
//                        return false;
//                    }
//                    //get interaction score from contact table
//                    String interaction_score_str = resultContact.getString("interaction_score");
//                    double interaction_score = Double.parseDouble(interaction_score_str);
////                    //put information into all map
////                    UserRow user = new UserRow(useridContact, interaction_score);
////                    allMap.put(useridContact, user);
//                    String tweet_list = null;
//
//                    //store the tweet that has required type
//                    if (type.equals("reply")) {
//                        tweet_list = resultContact.getString("reply_tweet_id_list");
//                        tweet_list = tweet_list.substring(1, tweet_list.length() - 1);
//                        tweet_list = tweet_list.replace("\\\"", "\"");
//                        tweet_list = tweet_list.replace("\\\\", "\\");
//                    } else if (type.equals("retweet")) {
//                        tweet_list = resultContact.getString("retweet_tweet_id_list");
//                        tweet_list = tweet_list.substring(1, tweet_list.length() - 1);
//                        tweet_list = tweet_list.replace("\\\"", "\"");
//                        tweet_list = tweet_list.replace("\\\\", "\\");
//
//                    } else if (type.equals("both")) {
//                        String reply_tweet_list = resultContact.getString("reply_tweet_id_list");
//                        if (reply_tweet_list.length() > 2) {
//                            reply_tweet_list = reply_tweet_list.substring(1, reply_tweet_list.length() - 1);
//                            reply_tweet_list = reply_tweet_list.replace("\\\"", "\"");
//                            reply_tweet_list = reply_tweet_list.replace("\\\\", "\\");
//                        }
//                        String retweet_tweet_list = resultContact.getString("retweet_tweet_id_list");
//                        if (retweet_tweet_list.length() > 2) {
//                            retweet_tweet_list = retweet_tweet_list.substring(1, retweet_tweet_list.length() - 1);
//                            retweet_tweet_list = retweet_tweet_list.replace("\\\"", "\"");
//                            retweet_tweet_list = retweet_tweet_list.replace("\\\\", "\\");
//                        }
//                        tweet_list = reply_tweet_list.substring(0, reply_tweet_list.length() - 1) + "," +
//                                retweet_tweet_list.substring(1);
//
//                    }
//
//                    if (!tweet_list.equals("")) {
//                        JSONArray jsonArr = new JSONArray(tweet_list);
//                        for (int i = 0; i < jsonArr.length(); i++) {
//                            if (!jsonArr.isNull(i)) {
//                                JSONObject jsonObj = jsonArr.getJSONObject(i);
//                                SendTweet sendtweet = new SendTweet(userid, useridContact,
//                                        jsonObj.get("create_time").toString(),
//                                        jsonObj.get("hash_tag").toString(),
//                                        jsonObj.get("tweet_id").toString(),
//                                        jsonObj.get("text").toString());
//                                tweetsContact.add(sendtweet);
//                                //put information into all map
//                                //interaction score is not null and key score is not null
//                                UserRow user = new UserRow(useridContact, interaction_score);
//                                allMap.put(useridContact, user);
//                            }
//                        }
//                    }
//
//
//                }
//            }
//            resultUser.close();
//            return true;
//        }
//        resultUser.close();
//        return false;
//
//
//    }
//
//
//    public void calHashTagScore(String userid) throws IOException, ParseException, SQLException {
//        HashMap<String, HashMap<String, Integer>> usersHashTag = new HashMap<>();
//        //popular_hashtags = readPopularHashtags();
//        //Set self-retweet and self-reply Score = 1
//
//        if (userList.size() != 0) {//still has non-zero interaction user maybe include request user himself
//            if (allMap.containsKey(userid)) {// if exist self-retweet and self-reply
//                //no need to add user id again into queryRowList
//                allMap.get(userid).setSame_tag_count(1);
//            } else {//add into userList, so that can get request user hashtag
//                userList.add(userid);
//            }
//            String sqlUserList = sqlUserList();
//            String sql = null;
//            if (userList.size() != 1 | (userList.size() == 1 && allMap.containsKey(userid))) {//except request user there are other user has non-zero score
//                sql = String.format("SELECT hash_tag,screen_name,description,user_id" +
//                        " FROM user WHERE user_key in %s", sqlUserList);
//                System.out.println(sql);
//                ResultSet resultUser = stmt.executeQuery(sql);
//                String hashTag = null;
//                String get_user_id = null;
//                String screen_name = null;
//                String description = null;
//                int n = 0;
//                while (resultUser.next()) {
//                    if (resultUser != null) {// if contain user information in user table
//                        hashTag = resultUser.getString("hash_tag");
//                        hashTag = hashTag.replace("\\\"", "\"");
//                        if (hashTag.length() > 2) {
//                            hashTag = hashTag.substring(1, hashTag.length() - 1);
//                        }
//                        get_user_id = resultUser.getString("user_id");
//                        screen_name = resultUser.getString("screen_name");
//                        description = resultUser.getString("description");
//                    } else {// if not contain user information in user table
//                        hashTag = "";
//                        screen_name = "";
//                        description = "";
//                        get_user_id = userList.get(n);
//
//                    }
//
//                    if (get_user_id.equals(userid)) {//if get userid = user id
//                        // check whether have reply-retweet
//                        // if yes, update the user information
//                        if (allMap.containsKey(userid)) {
//                            allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
//                        }
//                    } else {
//                        // if get userid!= user id, update information
//                        allMap.get(get_user_id).setDescriptionAndScreenName(description, screen_name);
//                    }
//                    if (!hashTag.equals("")) {
//                        HashMap<String, Integer> userHashTagFilter = userHashTag(hashTag);
//                        usersHashTag.put(get_user_id, userHashTagFilter);
//                    }
//                    n = n + 1;
//                }
//                HashMap<String, Integer> requestUserHashTag = usersHashTag.get(userid);
//                for (String hashtag : requestUserHashTag.keySet()) {
//                    Integer count = requestUserHashTag.get(hashtag);//find the count of request user_id
//                    for (String id : usersHashTag.keySet()) {
//                        if (!id.equals(userid)) {
//                            if (usersHashTag.get(id).containsKey(hashtag)) {// if contact user_id also has the hashtag
//                                int newCount = count + usersHashTag.get(id).get(hashtag);// add the count
//                                allMap.get(id).setSame_tag_count(newCount);//store in user
//                            }
//                        }
//                    }
//                }
//                resultUser.close();
//                countHashTag();//count all hashtags
//            }
//
//        }
//        System.out.println("end");
//    }
//
//    public String sqlUserList() {
//        ArrayList<String> rowkeyList = new ArrayList<>();
//        for (int i = 0; i < userList.size(); i++) {
////            StringBuilder b = new StringBuilder(userList.get(i));
////            String reverseUserId = b.reverse().toString();
//            rowkeyList.add(userList.get(i));
//        }
//        String userlist = rowkeyList.toString();
//        userlist = userlist.replace("[", "(");
//        userlist = userlist.replace("]", ")");
//        return userlist;
//    }
//
//    /**
//     * count final interaction score
//     */
//    public void countHashTag() {
//
//        for (String id : allMap.keySet()) {
//            allMap.get(id).calculateFinalHashTagScore();
//        }
//    }
//
//    public static HashMap<String, Integer> userHashTag(String hashTags) throws IOException, ParseException {
//        HashMap<String, Object> userHashTag = json2Map(hashTags);
//        HashMap<String, Integer> userUpdate = new HashMap<>();
//        //popular_hashtags = readPopularHashtags();
//        for (String hashtag : userHashTag.keySet()) {
//            //if the hashtag is not in the popular_hashtags
//            int count = (int) userHashTag.get(hashtag);
//            userUpdate.put(hashtag, count);
//
//        }
//        return userUpdate;
//
//    }
//
//
//    public void calKeyScore(String phrase, String hashtag) throws ParseException, IOException {
//        updateKWCountWithLatest(phrase, hashtag, tweetsContact);
//        for (Map.Entry<String, UserRow> user : allMap.entrySet()) {
//            UserRow userRow = user.getValue();
//            userList.add(userRow.calculateHashtagScore());
//        }
//    }
//
//    public void updateKWCountWithLatest(String phrase, String hashtag, ArrayList<SendTweet> tweetsReplyOrRetweet) throws ParseException, IOException {
//
//        for (SendTweet tweet : tweetsReplyOrRetweet) {
//            String userId = tweet.getReply_to_uid();
//            UserRow currentUser = allMap.get(userId);
//
//            if (currentUser != null) {
//                String content = tweet.getContent();
//                String hashTags = tweet.getHashtags();
//
//                // add phrase match:
//                int phrase_match = StringUtils.countMatches(content, phrase);
//                currentUser.addPhraseMatch(phrase_match);
//
//                // add hashtag match:
//                HashMap<String, Object> hashtagMap = json2Map(hashTags);
//                hashtag = hashtag.toLowerCase();
//                if (hashtagMap.containsKey(hashtag)) {
//                    int hashtag_match_int = (int) hashtagMap.get(hashtag);
//                    currentUser.addHashtagMatch(hashtag_match_int);
//                } else {
//                    currentUser.addHashtagMatch(0);
//                }
//
//                //update lasted contact:
//                currentUser.updateTweet(tweet.getContent(), tweet.getCreate_time(), tweet.getTweetId());
//            }
//        }
//    }
//
//
//    public String returnSortedUsers() {
//        String httpReply = "NoeatNosleep2021,554415272511";
//        //sort with tree map:
//
//        TreeMap<UserRow, String> treeMap = new TreeMap<>(new CompareScoreId());
//
//        for (Map.Entry<String, UserRow> user : allMap.entrySet()) {
//            UserRow userRow = user.getValue();
//            userRow.roundScore();
//            String userId = user.getKey();
//            treeMap.put(userRow, userId);
//        }
//
//        for (Map.Entry<UserRow, String> entry : treeMap.entrySet()) {
//            if (entry.getKey().getScore() != 0.0) {
//                httpReply += entry.getKey().toString();
//            }
//        }
//
//        return httpReply;
//    }
//
//    public static HashMap<String, Object> json2Map(String str) throws IOException {
//        HashMap<String, Object> response = new ObjectMapper().readValue(str, HashMap.class);
//        return response;
//    }
//
//    public static void main(String[] args) throws IOException, ParseException, JSONException {
//
//        StringBuilder b = new StringBuilder("430845087");
//        System.out.println(b.reverse().toString());
//        b = new StringBuilder("430845087");
//        System.out.println(b.reverse().toString());
//        b = new StringBuilder("1699533926");
//        System.out.println(b.reverse().toString());
//        b = new StringBuilder("743292229");
//        System.out.println(b.reverse().toString());
//
//    }
//}
