import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.Page;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
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

    private static AmazonDynamoDB client;

    private static DynamoDB dynamoDB;

    public TwitterWebService() throws IOException {
        client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDB = new DynamoDB(client);

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
                        Tweet tweet = new Tweet(dynamoDB);
                        if (tweet.calInteractionScore(userID, type)) {
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
