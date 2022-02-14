package twitter_utils;

import org.apache.hadoop.hbase.client.Admin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class Comparetest  implements Comparator<UserRow> {
    public int compare(UserRow u1, UserRow u2)
    {
        if (Double.compare(u2.getScore(), u1.getScore())!=0){
            return Double.compare(u2.getScore(), u1.getScore());
        }
        else{
            return u2.getUser_id().compareTo(u1.getUser_id());
        }
    }
}

public class testMain {

    Map<String, UserRow> allMap = new HashMap<String, UserRow>();

    public String returnSortedUsers(){
        String httpReply = "NoeatNosleep2021,554415272511";
        //sort with tree map:

        TreeMap<UserRow, String> treeMap = new TreeMap<>(new Comparetest());

        for(Map.Entry<String, UserRow> user : allMap.entrySet()) {
            UserRow userRow = user.getValue();
            userRow.roundScore();
            String userId = user.getKey();
            treeMap.put(userRow, userId);
        }

        for (Map.Entry<UserRow, String> entry : treeMap.entrySet()){
            if (entry.getKey().getScore() != 0.0){
                httpReply += entry.getKey().toString();
            }
        }

        return httpReply;
    }

    public static void main(String[] args) {
        testMain test = new testMain();

        UserRow old= new UserRow("old_hh",0);
        old.score=1.3;
        test.allMap.put("old_hh", old);

        UserRow fresh1=new UserRow("1",0);
        fresh1.score=1.3;
        test.allMap.put("1", fresh1);

        UserRow fresh2=new UserRow("5",0);
        fresh2.score=1.542352;
        test.allMap.put("2", fresh2);

        UserRow fresh3=new UserRow("3",0);
        fresh3.score=1.542355;
        test.allMap.put("3", fresh3);


        System.out.println(test.returnSortedUsers());
    }
}
