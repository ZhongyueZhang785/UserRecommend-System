package blockchain_utils;

import com.google.common.hash.Hashing;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class transcationBlock {
    private Long sig;
    private Long recv;
    private Long fee;
    private int amt;
    private String time;
    private Long send;
    private String hash;
    private  transient BigInteger e = BigInteger.valueOf(1097844002039l);
    private  transient BigInteger n = BigInteger.valueOf(1561906343821l);
    private  transient BigInteger d = BigInteger.valueOf(343710770439l);


    public String setTime(String time){
        Long t = Long.parseLong(time,10);
        BigInteger T = BigInteger.valueOf(t);
        T = T.add(BigInteger.valueOf(600000000000l));
        return T.toString();
    }
    public int calReard(int id){
        id = id/2;
        int numerator = (int)Math.pow(2, id);
        int amount = 500000000;
        return amount/numerator;

    }

    public void createRewardBlock(int id,String time){
        /*
            create a new reward block
         */
        this.recv = e.longValue();
        this.amt = calReard(id);
        this.time = setTime(time);
        setHash("rewardBlock");
    }

    public String getTime() {
        return time;
    }

    public Long getSend() {
        return send;
    }

    public void setSend() {

        this.send = e.longValue();
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public void setSig() {

        Long c = Long.parseLong(this.hash,16);
        BigInteger C = BigInteger.valueOf(c);
        BigInteger M = C.modPow(d,n);
        this.sig = M.longValue();
    }
    public boolean validHash() {
        BigInteger M = BigInteger.valueOf(sig);
        BigInteger e = BigInteger.valueOf(send);
        BigInteger C = M.modPow(e,n);
        long hash2 = C.longValue();

        String result = String.format("%08x",hash2);
        //System.out.println(result);

        if(hash.compareTo(result)==0)
            return true;
        else
            return false;


    }

    public void setHash(String type) {
        String origString = null;
        if (type.equals("newBlock")){
            origString = time+"|"+send+"|"+recv+"|"+amt+"|"+fee;
            //System.out.println(origString);
            }
        else{
            origString = time+"|"+"|"+recv+"|"+amt+"|";}
        //System.out.println(origString);
        String sha256hex = Hashing.sha256()
                .hashString(origString, StandardCharsets.UTF_8)
                .toString();
        this.hash = sha256hex.substring(0,8);
    }

    public String getHash() {
        return hash;
    }

    public Long getSig() {
        return sig;
    }

    public Long getFee() {
        return fee;
    }

    public Long getRecv() {
        return recv;
    }

    public int getAmt() {
        return amt;
    }

    public static void main(String[] args){

        transcationBlock ts2= new transcationBlock();
        ts2.time = "1582520423349514848";
        ts2.send = 1097844002039l;
        ts2.recv=   841025400571L;
        ts2.amt =  22976741;
        ts2.fee = 2071978l;
        ts2.setHash("newBlock");
        //System.out.println(ts2.hash);

        //System.out.println(ts2.validHash());



    }

}
