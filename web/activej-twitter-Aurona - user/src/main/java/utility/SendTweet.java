package utility;

import org.apache.commons.lang.StringUtils;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

/**
 * class store one tweet
 */
public class SendTweet {
    private String create_time;
    private String sender_uid;
    private String text;
    private String connection_to_uid;
    private String hashtags;
    private String tweet_id;

    public String getCreate_time() {
        return create_time;
    }

    public String getContent() {
        return text;
    }

    public String getHashtags() {
        return hashtags;
    }

    public String getReply_to_uid() {
        return connection_to_uid;
    }

    public String getTweetId() {
        return tweet_id;
    }
    SendTweet(String connection_to_uid,String create_time,String hashtags,String tweet_id,String text){
        this.connection_to_uid = connection_to_uid;
        this.create_time = create_time;
        this.hashtags = hashtags;
        this.tweet_id = tweet_id;
        this.text = text;
    }

    public int phraseMatch(String phaseRequest){
        int phrase_match = StringUtils.countMatches(text, phaseRequest);
        return phrase_match;
    }
    public int hashTagMatch(String hashTagRequest) throws IOException {
        HashMap<String, Object> hashtagMap = json2Map(hashtags);
        hashTagRequest = hashTagRequest.toLowerCase();
        if (hashtagMap.containsKey(hashTagRequest)) {
            int hashtag_match_int = (int) hashtagMap.get(hashTagRequest);
            return hashtag_match_int;
        } else {
            return 0;
        }

    }

    private HashMap<String, Object> json2Map(String str) throws IOException {
        HashMap<String, Object> response = new ObjectMapper().readValue(str, HashMap.class);
        return response;
    }



}
