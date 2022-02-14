package utility;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class cloudChain {
    public static String blockChainResponse(String encode){
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

}
