import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

import static io.activej.http.HttpMethod.GET;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.Executor;
import java.io.IOException;
import javax.sql.DataSource;
import java.sql.SQLException;

import static java.util.concurrent.Executors.newCachedThreadPool;

import blockchain_utils.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import twitter_utils.Tweet2;
import twitter_utils.VerifyRequest;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public final class P5Application extends HttpServerLauncher {

    private static Connection connection;
    private static String user;
    private static String password;
    private static String address;
    private static String dbName;
    private String url;

    public P5Application() throws IOException, SQLException {
        user = System.getenv("USER");
        //System.out.println(user);
        password = System.getenv("PASSWORD");
        //System.out.println(password);
        address = System.getenv("ADDRESS");
        //System.out.println(address);
        dbName = System.getenv("DB_NAME");
        //System.out.println(dbName);
        url = String.format("jdbc:mariadb://%s:3306/%s?connectTimeout=36000000&socketTimeout=36000000&user=%s&password=%s", address, dbName, user, password);
        //System.out.println(url);
//        connection = DriverManager.getConnection(url);

//        config = new HikariConfig();
//        config.setJdbcUrl(url);
//        config.setUsername(user);
//        config.setPassword(password);
//        config.setMinimumIdle(2);
//        config.setMaximumPoolSize(2);
//        config.setConnectionTimeout(36000000);
//        config.setIdleTimeout(36000000);
//        //config.addDataSourceProperty("maxLifetime",2);
    }

    @Provides
    Executor executor() {
        return newCachedThreadPool();
//        return newSingleThreadExecutor();
    }

    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create().map(GET, "/", request -> {
            return HttpResponse.ok200().withHtml(
                    "<p style=\"color:red;\"><b>activej-p4</b></p>I am working now."
            );
        }).map(GET, "/qrcode", request -> {
            String type = request.getQueryParameter("type");
            String data = request.getQueryParameter("data");
            final qr_utils.PairQR[] zigzagPairsV2 = qr_utils.ZigzagPair.getZigzagPairs(2);
            final qr_utils.PairQR[] zigzagPairsV1 = qr_utils.ZigzagPair.getZigzagPairs(1);
            final int[] logisticBitArray = qr_utils.LogisticBitArray.getLogisticBitArray();
            if (type.equals("encode")) {
                if (data.length() > 13) {
                    encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV2, logisticBitArray);
                    return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                } else {
                    encode.QRMap qrMap = new encode.QRMap(data, zigzagPairsV1, logisticBitArray);
                    return HttpResponse.ok200().withPlainText(qrMap.convertHex());
                }
            } else if (type.equals("decode")) {
                decode.QRMapDecode qrMapDecode = new decode.QRMapDecode(data, logisticBitArray);
                int[][] data32 = qrMapDecode.qrMap32;
                decode.BinarySquare binarySquare = new decode.BinarySquare(data32);
                binarySquare.findQRCode();
                int[][] mapFound = binarySquare.data;
                if (mapFound.length == 21) {
                    decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV1);
                    return HttpResponse.ok200().withPlainText(hexOutput.toString());
                } else {
                    decode.HexOutput hexOutput = new decode.HexOutput(mapFound, zigzagPairsV2);
                    return HttpResponse.ok200().withPlainText(hexOutput.toString());
                }
            } else {
                return HttpResponse.ok200().withPlainText(
                        String.format("Illegal request, type:%s, data:%s", type, data)
                );
            }
        }).map(GET, "/blockchain", request -> {
            String encode = request.getQueryParameter("cc");
            String response = cloudChain.blockChainResponse(encode);
            return HttpResponse.ok200().withPlainText(response);
        }).map(GET, "/twitter", request -> {

//            if (ds == null) {
//                ds = new HikariDataSource(config);
//            }
            if(connection==null){
                connection = DriverManager.getConnection(url);
            }else{
                if (connection.isClosed()) {
                    connection = DriverManager.getConnection(url);
                }
            }

            String userID = request.getQueryParameter("user_id");
            String type = request.getQueryParameter("type");
            String phrase = request.getQueryParameter("phrase");
            String hashTag = request.getQueryParameter("hashtag");

            VerifyRequest verifyRequest = new VerifyRequest(userID, type, phrase, hashTag);
            if (verifyRequest.verify()) {
                Tweet2 tweet = new Tweet2(connection, userID, type, phrase, hashTag);
                if (tweet.calInteractionScore()) {
                    String response = tweet.returnSortedUsers();
                    return HttpResponse.ok200().withPlainText(response);
                } else {
                    String response = "NoeatNosleep2021,554415272511\nINVALID";
                    return HttpResponse.ok200().withPlainText(response);
                }

            } else {
                String response = "NoeatNosleep2021,554415272511\nINVALID";
                return HttpResponse.ok200().withPlainText(response);
            }
        });

    }

    public static void main(String[] args) throws Exception {
        Launcher launcher = new P5Application();
        launcher.launch(args);
//        tweetConnectionStatic.close();
        connection.close();
    }
}