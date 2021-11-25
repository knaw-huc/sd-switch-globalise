package org.knaw.huc.sdswitch.server;

import io.javalin.Javalin;
import org.knaw.huc.sdswitch.server.config.Switch;
import org.knaw.huc.sdswitch.server.config.SwitchLoader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;

public class Application {
    public static void main(String[] args) throws Exception {
        final InputStream configStream = System.getProperty("config") != null
                ? new FileInputStream(System.getProperty("config"))
                : Application.class.getClassLoader().getResourceAsStream("config.xml");

        final SwitchLoader switchLoader = new SwitchLoader();
        final Set<Switch> switches = switchLoader.loadSwitches(configStream);

        final int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.enableDevLogging();
        }).start(port);

        for (Switch sw : switches) {
            app.get(sw.getUrlPattern(), sw::handle);
        }
    }
}
