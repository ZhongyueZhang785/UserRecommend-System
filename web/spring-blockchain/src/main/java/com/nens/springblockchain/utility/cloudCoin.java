package com.nens.springblockchain.utility;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class cloudCoin {
    private ArrayList<chainBlock> chain = new ArrayList<chainBlock>();//store the chain block
    private ArrayList<transcationBlock> new_tx=new ArrayList<transcationBlock>();
    private String new_target;
    private transient String lastRewardTimestamp;
    private transient String previousBlockHash = "00000000";


    cloudCoin(){}
    public Boolean validTest(){
        /*
            test if transactions are valid by timestamp and balance
         */
        String lastTimestamp = "000000";
        int chain_size = chain.size();
        for (com.nens.springblockchain.utility.chainBlock chainBlock : chain) {
            int all_tx_num = chainBlock.getAll_tx().size();
            for (int j = 0; j < all_tx_num; j++) {
                String nextTimestamp = chainBlock.getAll_tx().get(j).getTime();
                if (lastTimestamp.compareTo(nextTimestamp) >= 0)
                    // if the last time is later than next time, the transaction is invalid
                    return false;
                lastTimestamp = nextTimestamp;

            }
        }
        //store the necessary information
        if(chain_size!=0)//For the first block, it has a previous block hash of 00000000
            previousBlockHash = chain.get(chain_size-1).getHash();

        lastRewardTimestamp = lastTimestamp;

        for (int i=0; i<new_tx.size(); i++){
            String nextTimestamp = new_tx.get(i).getTime();
            if(lastTimestamp.compareTo(nextTimestamp)>=0)
                // if the last time is later than next time, the transaction is invalid
                return false;
            lastTimestamp = nextTimestamp;
            }
        return true;
    }
    public void setAddNewBlock(){
        /*
        set new block and add the new block to chain
         */
        chainBlock block = new chainBlock();
        int id = chain.size();
        int i = 0;
        String origString = String.valueOf(id)+"|"+previousBlockHash;
        String tx_hash = null;

        // set new block where account is a sender
        for ( i=0; i<new_tx.size(); i++){
            transcationBlock newTxBlock = new_tx.get(i);
            if (newTxBlock.getSend()==null){
                // send by the account
                newTxBlock.setSend();
                newTxBlock.setFee(0l);
                newTxBlock.setHash("newBlock");
                newTxBlock.setSig();
            }
            //add new block to all_tx
            origString = origString+"|"+newTxBlock.getHash();
            block.getAll_tx().add(newTxBlock);
        }

        //set reward block
        transcationBlock rewardBlock = new transcationBlock();
        rewardBlock.createRewardBlock(id,lastRewardTimestamp);
        origString = origString +"|"+rewardBlock.getHash();
        //add new block to all_tx
        block.getAll_tx().add(rewardBlock);
        block.setId(id);
        block.setTarget(new_target);
        block.setHashPow(origString);
        // add new transaction block to chain
        chain.add(block);
        new_target=null;
        new_tx=null;

    }

    public String outputData(ArrayList<chainBlock> chain){
        /*
            output the data
         */
        return "outputdata";
    }
    public String response(String encode){
        String team_id = "NoeatNosleep2021";
        String aws_id = "554415272511";
        //zlib and Base64
        byte[] decompressData = DecompressCompress.decompress(encode);
        byte[] byteOutput = new byte[0];
        String resultValue = new String(decompressData);

        Gson gson = new Gson();
        cloudCoin cloudcoin = gson.fromJson(resultValue, cloudCoin.class);

        if(cloudcoin.validTest()){//valid test

            cloudcoin.setAddNewBlock();
            String json = gson.toJson(cloudcoin);
            byteOutput = json.getBytes(StandardCharsets.UTF_8);
            byte[] compressData = DecompressCompress.compress(byteOutput);
            String response = new String(compressData);

            return (team_id+","+aws_id+"\n"+response);

        }
        else
            return (team_id+","+aws_id+"\n"+"INVALID");

    }

    public static void main(String[] args) {

        String endoce1 = "eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==";
        byte[] decompressData = DecompressCompress.decompress(endoce1);

        String resultValue = new String(decompressData);
        Gson gson = new Gson();
        cloudCoin cloudcoin = gson.fromJson(resultValue, cloudCoin.class);
        System.out.println(cloudcoin.validTest());
        cloudcoin.setAddNewBlock();
        String json = gson.toJson(cloudcoin);
        System.out.println(json);










    }

}
