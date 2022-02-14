package utility;

public class VerifyRequest {
    String userID;
    String type;
    String phrase;
    String hashTag;

    public VerifyRequest(String userID, String type, String phrase, String hashTag) {
        this.userID = userID;
        this.type = type;
        this.phrase = phrase;
        this.hashTag = hashTag;
    }

    public boolean verify(){
        if (this.userID==null || this.type==null || this.phrase==null || this.hashTag==null ){
            return false;
        }

        if (!(type.equals("reply") || type.equals("retweet") || type.equals("both"))) {
            return false;
        }

        return true;
    }
}
