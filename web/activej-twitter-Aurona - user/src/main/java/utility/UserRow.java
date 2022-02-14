package utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UserRow {
    // display
    String user_id;
    String screen_name;
    String description;
    String contact_tweet_text;
    String hashTag;
    // final score
    double score = 0.0;
    double key_score = 0.0;
    double hash_score = 0.0;
    // hashtag score calculation attribute:
    double same_tag_count = 0;
    // keyword score calculation attribute:
    String tweetId = "0";
    String create_time = "0";
    int key_phrase_match = 0;
    int key_hashtag_match = 0;
    boolean updatedKW = false;

    public  UserRow(){

    }
    public UserRow(String user_id, double score) {
        this.user_id = user_id;
        this.score = score;
    }

    public void setUserRow(String user_id, String hashTag, String description, String screen_name, double score) {
        this.user_id = user_id;
        this.hashTag = hashTag;
        this.description = description;
        this.screen_name = screen_name;
        this.score = score;
    }

    // getter:
    public String getHashTag() {
        return hashTag;
    }

    public String getUser_id() {
        return user_id;
    }

    public double getScore() {
        return score;
    }


    // setter;

    // interaction score calculation methods:

    // hashtag score calculation methods:

    // hashtag score calculation methods:
    public void addPhraseMatch(int count) {
        this.key_phrase_match += count;
        updatedKW = true;
    }

    public void addHashtagMatch(int count) {
        this.key_hashtag_match += count;
        updatedKW = true;
    }

    public void updateTweet(String content, String timestamp, String newTweetId) {
        if (this.create_time.compareTo(timestamp) < 0) {
            this.contact_tweet_text = content;
            this.create_time = timestamp;
            this.tweetId = newTweetId;
        } else if (this.create_time.equals(timestamp)) {
            if (Long.valueOf(this.tweetId) < Long.valueOf(newTweetId)) {
                this.contact_tweet_text = content;
                this.create_time = timestamp;
                this.tweetId = newTweetId;
            }
        }
    }



    public Boolean calculateKeyScore() {
        if (updatedKW) {
            this.key_score =  (1 + Math.log(1 + key_phrase_match + key_hashtag_match));
            return true;
        } else {
            this.score = 0.0;
        }
        return false;

    }


    public void setSame_tag_count(Integer num) {
        this.same_tag_count = same_tag_count + num;

    }

    public void calculateFinalHashTagScore() {
        if (same_tag_count > 10) {
            this.hash_score = 1 + Math.log(1 + same_tag_count - 10);
        } else {
            this.hash_score = 1;
        }
    }


    public String toString() {
        if (description != null) {
            description = description.replace("\\n", "\n");
            description = description.replace("\\r", "\r");
        }
        if (contact_tweet_text != null) {
            contact_tweet_text = contact_tweet_text.replace("\\n", "\n");
            contact_tweet_text = contact_tweet_text.replace("\\r", "\r");
        }
        if (screen_name.equals("\"\"")) {
            screen_name = "";
        }
        if (description.equals("\"\"")) {
            description = "";
        }
        //return "\n" + user_id + "\t" + screen_name + "\t" + description + "\t" + contact_tweet_text +"\t"+score;
        return "\n" + user_id + "\t" + screen_name + "\t" + description + "\t" + contact_tweet_text;
    }

    public void roundScore() {
        score = score*key_score*hash_score;
        BigDecimal rounded = new BigDecimal(score);
        rounded = rounded.setScale(5, RoundingMode.HALF_UP);
        score = rounded.doubleValue();
//         score = Math.round(score);
    }


}
