import io.activej.http.AsyncServlet;
import io.activej.http.HttpResponse;
import io.activej.http.RoutingServlet;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.HttpServerLauncher;

import java.util.concurrent.Executor;

import static io.activej.http.HttpMethod.GET;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import utility.*;

public final class activejBlockChain extends HttpServerLauncher {

    @Provides
    Executor executor() {
        return newSingleThreadExecutor();
    }

    //[START REGION_1]
    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create()
                .map(GET, "/blockchain", request -> {
                    String encode = request.getQueryParameter("cc");
                    String response = cloudChain.blockChainResponse(encode);
                    return HttpResponse.ok200().withPlainText(response);
                });

    }

    //[END REGION_1]

    public static void main(String[] args) throws Exception {
        Launcher launcher = new activejBlockChain();
        launcher.launch(args);
    }
}