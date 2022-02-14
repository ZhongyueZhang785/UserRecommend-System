package com.nens.springblockchain.utility;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;

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

    public void setHash(String type) {
        String origString = null;
        if (type.equals("newBlock"))
            origString = time+"|"+send+"|"+recv+"|"+amt+"|"+fee;
        else
            origString = time+"|"+"|"+recv+"|"+amt+"|";
        //System.out.println(origString);
        String sha256hex = Hashing.sha256()
                .hashString(origString, StandardCharsets.UTF_8)
                .toString();
        this.hash = sha256hex.substring(0,8);
    }

    public String getHash() {
        return hash;
    }

    public static void main(String[] args){
        transcationBlock ts= new transcationBlock();
        ts.time = "1582521645744862608";
        ts.send= 1097844002039l;
        ts.recv = 895456882897l;
        ts.fee = 0l;
        ts.amt = 34263741;
        ts.setHash("newBlock");
        ts.setSig();
        Gson gson = new Gson();
        String json = gson.toJson(ts);
        System.out.println(json);

        transcationBlock ts2= new transcationBlock();
        ts2.recv=895456882897l;
        ts2.amt = 34263741;
        ts2.createRewardBlock(3,"1582521603026667063");
        String json2 = gson.toJson(ts2);
        System.out.println(json2);



    }

}
