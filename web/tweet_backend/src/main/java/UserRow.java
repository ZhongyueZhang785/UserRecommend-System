
public class UserRow {
    // display
    String user_id;
    String screen_name;
    String description;
    String contact_tweet_text;

    // final score
    double score;

    // interaction score calculation attribute:
    int reply_num = 0;
    int retweet_num = 0;
    // hashtag score calculation attribute:
    double same_tag_count = 0;
    // keyword score calculation attribute:
    String create_time = "0";
    int key_phrase_match;
    int key_hashtag_match;


    public UserRow(String user_id) {
        this.user_id = user_id;
    }
    // getter:

    // setter;

    // interaction score calculation methods:

    // hashtag score calculation methods:

    // hashtag score calculation methods:
    public void addPhraseMatch(int count){
        this.key_phrase_match += count;
    }

    public void addHashtagMatch(int count){
        this.key_hashtag_match += count;
    }

    public void updateTweet(String content, String timestamp){
        if(this.create_time.compareTo(timestamp) < 0){
            this.contact_tweet_text=content;
            this.create_time=timestamp;
        }
    }

    public String calculateHashtagScore(){
        this.score = this.score*(Math.log(1 + key_phrase_match + key_hashtag_match));
        if (score != 0.0){
            return ("|" + user_id);
        }
        return "";
    }

    public void setReply_num() {
        reply_num++;
    }

    public void setRetweet_num() {
        retweet_num++;
    }

    public void calculateInteractionScore() {
        this.score = Math.log(1 + 2 * reply_num + retweet_num);
    }
    public void setSame_tag_count(Double num){
        this.same_tag_count = same_tag_count+num;

    }
    public void calculateHashTagScore(){
        if(same_tag_count>10){
            double hashtag_score = 1+Math.log(1+same_tag_count-10);
            score = score*hashtag_score;
        }
        else{
            score = score*1;
        }
    }

    public void setDescriptionAndScreenName(String description,String screen_name) {
        this.description = description;
        this.screen_name = screen_name;
    }

}
