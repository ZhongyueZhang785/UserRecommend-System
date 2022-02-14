package blockchain_utils;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class cloudCoin {
    private ArrayList<chainBlock> chain = new ArrayList<chainBlock>();//store the chain block
    private ArrayList<transcationBlock> new_tx = new ArrayList<transcationBlock>();
    private String new_target;
    private String lastRewardTimestamp;
    private String previousBlockHash = "00000000";
    private Map<Long, Integer> balance = new HashMap<Long, Integer>();
    private Long fee = 0l;

    cloudCoin() {
    }

    public Boolean calBalance(transcationBlock tb) {
        String requestHash = tb.getHash();
        Long fee1 = 0l;
        if (tb.getFee() != null) {
            fee1 = tb.getFee();//calculate fee
            if (fee1 < 0)//judge whether the fee is negative
                return false;
            fee = fee + fee1;
        }

        int amt = tb.getAmt();
        if (amt < 0)//judge whether the amt is negative
            return false;
        Long send = tb.getSend();
        Long rec = tb.getRecv();

        if (send != null) {//normal transaction
            //valid sig
            if (!tb.validHash()) {
                System.out.println("ValidHash");
                return false;
            }
            tb.setHash("newBlock");
            if (!tb.getHash().equals(requestHash)) {
                System.out.println(tb.getHash() + " request: " + requestHash);
                //valid transaction hash
                return false;
            }

            // if send already in the balance
            if (balance.containsKey(send)) {
                //calculate the new amt
                int new_amt = (int) (balance.get(send) - amt - fee1);
                if (new_amt < 0)
                    return false;
                else
                    balance.put(send, new_amt);
            } else// if send not in balance definitely wrong
                return false;

        }
        //normal transaction + send transcation
        //System.out.println(rec);
        if (balance.containsKey(rec)) {
            // if rec has been in balance
            int new_amt = 0;
            if (send == null) {//the send transaction
                //calucate the new amt with fee and amt
                tb.setHash("sendTransaction");
                if (requestHash != null) {// send transcation in new_tx
                    if (!tb.getHash().equals(requestHash)) {
                        //valid transaction hash
                        return false;
                    }
                }
                new_amt = (int) (balance.get(rec) + fee + amt);
                balance.put(rec, new_amt);
            } else {
                //the normal transaction
                //cal without fee
                new_amt = balance.get(rec) + amt;
                balance.put(rec, new_amt);
            }
        } else {
            //if rec has not been in balance
            if (send == null) {//the reward block
                balance.put(rec, (int) (amt + fee));
            } else//normal transaction
                balance.put(rec, amt);

        }

        return true;

    }

    public Boolean validTest() {
        /*
            test if transactions are valid by timestamp and balance
         */


        String lastTimestamp = "000000";
        int chain_size = chain.size();
        int id = -1;

        for (blockchain_utils.chainBlock chainBlock : chain) {
            id = id + 1;
            fee = 0l;
            int all_tx_num = chainBlock.getAll_tx().size();
            if (chainBlock.getHash().compareTo(chainBlock.getTarget()) >= 0)//test block hash
            {
                System.out.println("hash larger than target");
                return false;
            }
            if (chainBlock.getId() != id) {
                System.out.println("id failure");
                return false;
            }
            for (int j = 0; j < all_tx_num; j++) {
                String nextTimestamp = chainBlock.getAll_tx().get(j).getTime();
                if (lastTimestamp.compareTo(nextTimestamp) >= 0)// if the last time is later than next time, the transaction is invalid
                {
                    System.out.println("time sequen in old tx");
                    return false;
                }
                lastTimestamp = nextTimestamp;
                if (!calBalance(chainBlock.getAll_tx().get(j)))//balance not account
                {
                    System.out.println("j:" + j + "\n" + chainBlock.getAll_tx().get(j).getSig());
                    System.out.println("balance not right in old");
                    return false;
                }

                //modification:
                int rewardId = chainBlock.getId() / 2;
                int rewardAmount = chainBlock.getAll_tx().get(chainBlock.getAll_tx().size() - 1).getAmt();
                Long rewardSender = chainBlock.getAll_tx().get(chainBlock.getAll_tx().size() - 1).getSend();

                if (rewardSender != null) {
                    return false;
                }

                int numerator = (int) Math.pow(2, rewardId);

                int rightAmount = 500000000 / numerator;

                if (rightAmount != rewardAmount) {
                    return false;
                }
            }

        }
        //store the necessary information
        if (chain_size != 0)//For the first block, it has a previous block hash of 00000000
            previousBlockHash = chain.get(chain_size - 1).getHash();

        lastRewardTimestamp = lastTimestamp;

        for (int i = 0; i < new_tx.size(); i++) {
            fee = 0l;
            String nextTimestamp = new_tx.get(i).getTime();
            if (lastTimestamp.compareTo(nextTimestamp) >= 0)// if the last time is later than next time, the transaction is invalid
            {
                System.out.println("time sequential");
                return false;
            }
            lastTimestamp = nextTimestamp;
            if (i < new_tx.size() - 1) {
                if (calBalance(new_tx.get(i)) == false) {//balance not right
                    return false;
                }
            } else {
                if (balance.containsKey(1097844002039l)) {
                    if ((balance.get(1097844002039l) - new_tx.get(i).getAmt()) < 0) {
                        System.out.println("balance in nex tx");
                        return false;
                    }
                } else {
                    System.out.println("sender is not our account in new tx");
                    return false;
                }

            }

        }

        return true;
    }

    public void setAddNewBlock() {
        /*
        set new block and add the new block to chain
         */
        chainBlock block = new chainBlock();
        int id = chain.size();
        int i = 0;
        String origString = String.valueOf(id) + "|" + previousBlockHash;
        String tx_hash = null;

        // set new block where account is a sender
        for (i = 0; i < new_tx.size(); i++) {
            transcationBlock newTxBlock = new_tx.get(i);
            if (newTxBlock.getSend() == null) {
                // send by the account
                newTxBlock.setSend();
                newTxBlock.setFee(0l);
                newTxBlock.setHash("newBlock");
                newTxBlock.setSig();
            }
            //add new block to all_tx
            origString = origString + "|" + newTxBlock.getHash();
            block.getAll_tx().add(newTxBlock);
        }

        //set reward block
        transcationBlock rewardBlock = new transcationBlock();
        //System.out.println(lastRewardTimestamp);
        rewardBlock.createRewardBlock(id, lastRewardTimestamp);
        origString = origString + "|" + rewardBlock.getHash();
        //add new block to all_tx
        block.getAll_tx().add(rewardBlock);
        block.setId(id);
        block.setTarget(new_target);
        block.setHashPow(origString);
        // add new transaction block to chain
        chain.add(block);

        new_target = null;
        new_tx = null;
        balance = null;
        lastRewardTimestamp = null;
        fee = null;
        previousBlockHash = null;


    }

    public String response(String encode) {
        String team_id = "NoeatNosleep2021";
        String aws_id = "554415272511";
        //zlib and Base64
        byte[] decompressData = DecompressCompress.decompress(encode);
        byte[] byteOutput = new byte[0];
        String resultValue = new String(decompressData);

        Gson gson = new Gson();
        cloudCoin cloudcoin = gson.fromJson(resultValue, cloudCoin.class);

        if (cloudcoin.validTest()) {//valid test

            cloudcoin.setAddNewBlock();
            String json = gson.toJson(cloudcoin);
            byteOutput = json.getBytes(StandardCharsets.UTF_8);
            byte[] compressData = DecompressCompress.compress(byteOutput);
            String response = new String(compressData);

            return (team_id + "," + aws_id + "\n" + response);

        } else
            return (team_id + "," + aws_id + "\n" + "INVALID");

    }

    public static <Stirng> void main(String[] args) {

        String endoce1 = "eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==";
        byte[] decompressData = DecompressCompress.decompress(endoce1);

        String resultValue = new String(decompressData);
        Gson gson = new Gson();
        cloudCoin cloudcoin = gson.fromJson(resultValue, cloudCoin.class);

        cloudcoin.validTest();
        System.out.println(cloudcoin.validTest());
        cloudcoin.setAddNewBlock();
        String result = gson.toJson(cloudcoin);
        System.out.println(result);


    }

}
