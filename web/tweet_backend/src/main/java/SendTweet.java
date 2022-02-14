/**
 * class store one tweet
 */
public class SendTweet {
    private String create_time;
    private String sender_uid;
    private String content;
    private String reply_to_uid;
    private String retweet_to_uid;
    private String hashtags;

    public String getSender_uid() {
        return sender_uid;
    }

    public String getCreate_time() {
        return create_time;
    }

    public String getContent() {
        return content;
    }

    public String getHashtags() {
        return hashtags;
    }

    public String getReply_to_uid() {
        return reply_to_uid;
    }

    public String getRetweet_to_uid() {
        return retweet_to_uid;
    }

    public void setReply(String sender_uid,String content,String reply_to_uid,String hashtags,String create_time){
        this.sender_uid = sender_uid;
        this.content = content;
        this.reply_to_uid = reply_to_uid;
        this.hashtags = hashtags;
        this.create_time = create_time;
    }

    public void setRetweet(String sender_uid,String content,String retweet_to_uid,String hashtags,String create_time){
        this.sender_uid = sender_uid;
        this.content = content;
        this.retweet_to_uid = retweet_to_uid;
        this.hashtags = hashtags;
        this.create_time = create_time;
    }
}
