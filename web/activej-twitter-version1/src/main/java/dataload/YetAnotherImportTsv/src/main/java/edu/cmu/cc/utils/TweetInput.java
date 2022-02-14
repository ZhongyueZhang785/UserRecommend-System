package edu.cmu.cc.utils;

public class TweetInput {
    private String create_time = null;
    private String text = null;
    private String reply_to_id = null;
    private String retweet_to_id = null;
    private String user_id = null;
    private String tweet_id= null;
    private String hash_tag = null;

    public String getCreate_time() {
        return create_time;
    }

    public String getHash_tag() {
        return hash_tag;
    }

    public String getReply_to_id() {
        return reply_to_id;
    }

    public String getRetweet_to_id() {
        return retweet_to_id;
    }

    public String getText() {
        return text;
    }

    public String getTweet_id() {
        return tweet_id;
    }

    public String getUser_id() {
        return user_id;
    }

}
