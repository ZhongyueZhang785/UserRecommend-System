import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;
import utility.Tweet2;
import utility.VerifyRequest;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;

import static io.activej.http.HttpMethod.GET;
import static java.util.concurrent.Executors.newSingleThreadExecutor;


public final class TwitterWebService extends HttpServerLauncher {

    private static Connection connection;
//    private static String user = "admin";
//    private static String password = "ccnn2021!";
//    private static String address = "aurora-from-snapshot.cf6apywz25dq.us-east-1.rds.amazonaws.com";
//    private static String dbName = "twitter_aurora";
    private static String user;
    private static String password;
    private static String address;
    private static String dbName;

    public TwitterWebService() throws IOException, SQLException {
        user = System.getenv("USER");
        //System.out.println(user);
        password = System.getenv("PASSWORD");
        //System.out.println(password);
        address = System.getenv("ADDRESS");
        //System.out.println(address);
        dbName = System.getenv("DB_NAME");
        //System.out.println(dbName);
        String url = String.format("jdbc:mariadb://%s:3306/%s",address,dbName);
        //System.out.println(url);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(2);
        config.setConnectionTimeout(36000000);
        config.setIdleTimeout(36000000);
        //config.addDataSourceProperty("maxLifetime",2);

        DataSource ds = new HikariDataSource(config);
        connection  = ds.getConnection();
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
                        Tweet2 tweet = new Tweet2(connection,userID,type,phrase,hashTag);
                        if (tweet.calInteractionScore()) {
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
        connection.close();
    }
}
