package twitter_utils;

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
    SendTweet(String sender_uid,String connection_to_uid,String create_time,String hashtags,String tweet_id,String text){
        this.sender_uid = sender_uid;
        this.connection_to_uid = connection_to_uid;
        this.create_time = create_time;
        this.hashtags = hashtags;
        this.tweet_id = tweet_id;
        this.text = text;
    }

}
