import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

import java.util.concurrent.Executor;

import static io.activej.http.HttpMethod.GET;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.Executors.newCachedThreadPool;

import blockchain_utils.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

import org.apache.hadoop.hbase.client.HBaseAdmin;
import twitter_utils.*;


public final class P2Application extends HttpServerLauncher {
//    private static Connection tweetConnectionStatic;

    private Connection tweetConnection;

    private Configuration conf;

    public P2Application() throws IOException {
        conf = HBaseConfiguration.create();
//        conf.set("hbase.client.retries.number", "3");
//        String zkAddr = "ip-172-20-52-54.ec2.internal";
        String zkAddr = System.getenv("ZK_ADDR");
        conf.set("hbase.master", zkAddr + ":16000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
//        conf.set("hbase.zookeeper.property.clientport", "2181");
        conf.set("hbase.rpc.timeout", "90000000");
        conf.set("hbase.client.scanner.timeout.period", "90000000");
//        tweetConnection = ConnectionFactory.createConnection(conf);
//        tweetConnectionStatic = tweetConnection;

    }

    @Provides
    Executor executor() {
        return newCachedThreadPool();
//        return newSingleThreadExecutor();
    }

    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create().map(GET, "/", request -> {
//            return HttpResponse.ok200().withPlainText("Greetings. This is <b>activej-p2</b> service. I am working now.");
            return HttpResponse.ok200().withHtml(
                    "<p style=\"color:red;\"><b>activej-p2</b></p>I am working now."
            );
        }).map(GET, "/qrcode", request -> {
            String type = request.getQueryParameter("type");
            String data = request.getQueryParameter("data");
            final qr_utils.PairQR[] zigzagPairsV2 = qr_utils.ZigzagPair.getZigzagPairs(2);
            final qr_utils.PairQR[] zigzagPairsV1 = qr_utils.ZigzagPair.getZigzagPairs(1);
            final int[] logisticBitArray = qr_utils.LogisticBitArray.getLogisticBitArray();
            if (type.equals("encode")) {
//            String data = "CC Team";
                if (data.length() > 13) {
                    encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV2, logisticBitArray);
                    System.out.println(qrMap.convertHex());
                    return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                } else {
                    encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV1, logisticBitArray);
                    System.out.println(qrMap.convertHex());
                    return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                }

            } else if (type.equals("decode")) {
//            String data = "0x2b23d6830x15a0de0d0x744784010x29e880700xfe1adf5c0xb96061290x1127b67c0x311690430xc63153140xf6e00650x92d3960b0xf59a79070x704e73d40x977fd8090xf516e98a0x3e0c19f10xac626d040x6a3e58650xca85aa3e0x6266b640x842ddcb40x4e7c879c0x85dd21240x3afae3dc0xe07908a70x664685970xb38246f70x511908330x40a111ee0xc12c8fd10x82984c520x4ddee6f6";
                decode.QRMapDecode qrMapDecode = new decode.QRMapDecode(data, logisticBitArray);
                int[][] data32 = qrMapDecode.qrMap32;
                decode.BinarySquare binarySquare = new decode.BinarySquare(data32);
                binarySquare.findQRCode();
                int[][] mapFound = binarySquare.data;
                if (mapFound.length == 21) {
                    decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV1);
                    System.out.println(hexOutput.toString());
                    return HttpResponse.ok200().withPlainText(hexOutput.toString());
                } else {
                    decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV2);
                    System.out.println(hexOutput.toString());
                    return HttpResponse.ok200().withPlainText(hexOutput.toString());
                }
            } else {
                System.out.println(String.format("Illegal request, type:%s, data:%s", type, data));
                return HttpResponse.ok200().withPlainText(
                        String.format("Illegal request, type:%s, data:%s", type, data)
                );
            }
        }).map(GET, "/blockchain", request -> {
            String encode = request.getQueryParameter("cc");
            String response = cloudChain.blockChainResponse(encode);
            return HttpResponse.ok200().withPlainText(response);
        }).map(GET, "/twitter", request -> {
            if (tweetConnection == null || tweetConnection.isClosed()) {
                tweetConnection = ConnectionFactory.createConnection(conf);
            }

            String userID = request.getQueryParameter("user_id");
            String type = request.getQueryParameter("type");
            String phrase = request.getQueryParameter("phrase");
            String hashTag = request.getQueryParameter("hashtag");

            VerifyRequest verifyRequest = new VerifyRequest(userID, type, phrase, hashTag);
            if (verifyRequest.verify()) {
                Tweet tweet = new Tweet(tweetConnection);
                if (tweet.calInteractionScore(userID, type)) {
                    tweet.calKeyScore(phrase, hashTag);
                    tweet.calHashTagScore(userID);
                    String response = tweet.returnSortedUsers();
                    return HttpResponse.ok200().withPlainText(response);
                } else {
                    String response = "NoeatNosleep2021,554415272511\nINVALID";
                    return HttpResponse.ok200().withPlainText(response);
                }

//                HBaseAdmin admin = new HBaseAdmin(conf);
//                TableName tableNameTweet = TableName.valueOf("user");
//                if (admin.tableExists(tableNameTweet)) {
//                    String response = "exists";
//                    return HttpResponse.ok200().withPlainText(response);
//                } else {
//                    String response = "12NoeatNosleep2021,554415272511\nINVALID";
//                    return HttpResponse.ok200().withPlainText(response);
//                }
            } else {
                String response = "NoeatNosleep2021,554415272511\nINVALID";
                return HttpResponse.ok200().withPlainText(response);
            }
        });

    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new P2Application();
        launcher.launch(args);
//        tweetConnectionStatic.close();
    }
}