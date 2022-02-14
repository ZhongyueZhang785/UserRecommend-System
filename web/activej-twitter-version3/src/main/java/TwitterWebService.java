import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import utility.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Executor;

import static io.activej.http.HttpMethod.GET;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;


public final class TwitterWebService extends HttpServerLauncher {

    private static Connection tweetConnectionStatic;

    private Connection tweetConnection;

    private Configuration conf;

    private static String fileName = "src\\main\\java\\utility\\popular_hashtags.txt";
    //store the filter hashtags
    private static HashMap<String, Integer> popular_hashtags;

    public static HashMap<String, Integer> readPopularHashtags() throws FileNotFoundException {
        //Waiting for change
        HashMap<String, Integer> popular_hashtags = new HashMap<String, Integer>();
        try (Scanner sc = new Scanner(new FileReader(fileName))) {
            while (sc.hasNextLine()) {  //read by line
                String line = sc.nextLine();
                popular_hashtags.put(line, 1);
            }
        }
        return popular_hashtags;
    }

    public TwitterWebService() throws IOException {
        conf = HBaseConfiguration.create();
        String zkAddr = "ip-172-31-29-131.ec2.internal";
        //String zkAddr = System.getenv("ZK_ADDR");
        conf.set("hbase.master", zkAddr + ":16000");
        conf.set("hbase.zookeeper.quorum", zkAddr);
//        conf.set("hbase.zookeeper.property.clientport", "2181");
        conf.set("hbase.rpc.timeout", "90000000");
        conf.set("hbase.client.scanner.timeout.period", "90000000");
        tweetConnection = ConnectionFactory.createConnection(conf);
        tweetConnectionStatic = tweetConnection;
        popular_hashtags = readPopularHashtags();

    }

    @Provides
    Executor executor() {
        return newSingleThreadExecutor();
    }

    //[START REGION_1]
    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create()
                .map(GET, "/twitter", request -> {
                    String userID = request.getQueryParameter("user_id");
                    String type = request.getQueryParameter("type");
                    String phrase = request.getQueryParameter("phrase");
                    String hashTag = request.getQueryParameter("hashtag");

                    VerifyRequest verifyRequest = new VerifyRequest(userID, type, phrase, hashTag);
                    if (verifyRequest.verify()) {
                        Tweet tweet = new Tweet(tweetConnection,popular_hashtags);
                        if (tweet.calInteractionScore(userID,type)) {
                            tweet.calKeyScore(phrase, hashTag);
                            tweet.calHashTagScore(userID);

                            String response = tweet.returnSortedUsers();
                            return HttpResponse.ok200().withPlainText(response);
                        } else {
                            String response = "NoeatNosleep2021,554415272511\nINVALID";
                            return HttpResponse.ok200().withPlainText(response);
                        }

//                        HBaseAdmin admin = new HBaseAdmin(conf);
//                        TableName tableNameTweet = TableName.valueOf("tweet");
//                        if (admin.tableExists(tableNameTweet)) {
//                            String response = "exists";
//                            return HttpResponse.ok200().withPlainText(response);
//                        } else {
//                            String response = "12NoeatNosleep2021,554415272511\nINVALID";
//                            return HttpResponse.ok200().withPlainText(response);
//                        }
                    } else {
                        String response = "NoeatNosleep2021,554415272511\nINVALID";
                        return HttpResponse.ok200().withPlainText(response);
                    }
                });
    }
    //[END REGION_1]

    public static void main(String[] args) throws Exception {
        Launcher launcher = new TwitterWebService();
        launcher.launch(args);
        // close connection;
        tweetConnectionStatic.close();
    }
}
