package blockchain_utils;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.RandomStringUtils;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class chainBlock {
    private ArrayList<transcationBlock> all_tx = new ArrayList<transcationBlock>();//store the transcationBlock
    private String pow;
    private int id;
    private String hash;
    private String target;


    public void setHashPow(String origString){
        /*
            calculate and set the hash and pow of new block
         */
        String sha256hex = Hashing.sha256()
                .hashString(origString, StandardCharsets.UTF_8)
                .toString();
        while(true){
            //random generate pow
            pow =RandomStringUtils.randomAlphanumeric(3);
            origString = sha256hex+pow;
            hash = Hashing.sha256().hashString(origString, StandardCharsets.UTF_8).toString().substring(0,8);
            if(hash.compareTo(target)<0)
                //block hash is lexicographically smaller than the block's hash target
                break;
        }

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ArrayList<transcationBlock> getAll_tx() {
        return all_tx;
    }

    public String getHash() {
        return hash;
    }

    public String getTarget() {
        return target;
    }

    public int getId() {
        return id;
    }

    public static void main(String[] args){
        chainBlock cb = new chainBlock();
        cb.target="007";
        cb.setHashPow("3|00288a38|1fb48c71|a25b1fc9|1ea23984");
//        System.out.println("pow: "+cb.pow);
//        System.out.println("hash: "+cb.hash);
//        System.out.println(cb.hash.compareTo(cb.target));

    }
}
