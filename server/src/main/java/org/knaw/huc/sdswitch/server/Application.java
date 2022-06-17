package org.knaw.huc.sdswitch.server;

import io.javalin.Javalin;
import io.javalin.core.util.JavalinLogger;
import org.knaw.huc.sdswitch.server.config.Router;
import org.knaw.huc.sdswitch.server.config.Switch;
import org.knaw.huc.sdswitch.server.config.SwitchLoader;
import org.knaw.huc.sdswitch.server.config.SwitchRoute;
import org.knaw.huc.sdswitch.server.security.OpenID;
import org.knaw.huc.sdswitch.server.security.SecurityRouter;

import javax.ws.rs.core.UriBuilder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Application {
    public static void main(String[] args) throws Exception {
        final InputStream configStream = System.getenv().get("CONFIG") != null
                ? new FileInputStream(System.getenv().get("CONFIG"))
                : Application.class.getClassLoader().getResourceAsStream("config.xml");

        final SwitchLoader switchLoader = new SwitchLoader();
        final Map<SwitchRoute, Set<Switch<?>>> switches = switchLoader.loadSwitches(configStream);

        OpenID openID = null;
        if (System.getenv().get("OIDC_SERVER") != null &&
                System.getenv().get("OIDC_CLIENT_ID") != null &&
                System.getenv().get("OIDC_CLIENT_SECRET") != null) {
            final URI oidcServer = URI.create(System.getenv().get("OIDC_SERVER"));
            final URI applicationUrl = URI.create(System.getenv().getOrDefault("APPLICATION_URL", "http://localhost"));
            final URI redirectUrl = UriBuilder.fromUri(applicationUrl).path("/redirect").build();

            final String clientId = System.getenv().get("OIDC_CLIENT_ID");
            final String clientSecret = System.getenv().get("OIDC_CLIENT_SECRET");

            openID = new OpenID(oidcServer, redirectUrl, clientId, clientSecret);
        }
        final Optional<SecurityRouter> securityRouter =
                Optional.ofNullable(openID != null ? new SecurityRouter(openID) : null);

        final int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            securityRouter.ifPresent(config::accessManager);
        }).start(port);

        securityRouter.ifPresent(router -> router.registerRoutes(app));

        for (SwitchRoute switchRoute : switches.keySet().stream().sorted().toList()) {
            new Router(app, switchRoute, switches.get(switchRoute));
            JavalinLogger.info("Configured a new switch for URL " + switchRoute.urlPattern());
        }
    }
}
