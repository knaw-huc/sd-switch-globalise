package nl.knaw.huc.sdswitch.server;

import io.javalin.Javalin;
import io.javalin.validation.ValidationException;
import nl.knaw.huc.sdswitch.server.queue.TaskQueue;
import nl.knaw.huc.sdswitch.server.routing.SwitchRouter;
import nl.knaw.huc.sdswitch.server.routing.Switch;
import nl.knaw.huc.sdswitch.server.config.SwitchException;
import nl.knaw.huc.sdswitch.server.config.SwitchLoader;
import nl.knaw.huc.sdswitch.server.routing.TaskRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static nl.knaw.huc.sdswitch.server.util.Server.PORT;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private final Javalin app;
    private final TaskQueue taskQueue;

    public Application() throws Exception {
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.validation.register(UUID.class, UUID::fromString);
        }).start(PORT);

        app.exception(ValidationException.class, (e, ctx) -> ctx.result(e.getMessage()).status(400));

        final Path tasksDir = System.getenv().containsKey("TASKS_DIR")
                ? Path.of(System.getenv().get("TASKS_DIR"))
                : Files.createTempDirectory("sdswitch_tasks");
        taskQueue = new TaskQueue(tasksDir);
        new TaskRouter(app, taskQueue);

        setUpSwitchRoutes();
    }

    public static void main(String[] args) throws Exception {
        new Application();
    }

    private void setUpSwitchRoutes() throws FileNotFoundException, SwitchException {
        final InputStream configStream = System.getenv().get("CONFIG") != null
                ? new FileInputStream(System.getenv().get("CONFIG"))
                : Application.class.getClassLoader().getResourceAsStream("config.yml");

        final SwitchLoader switchLoader = new SwitchLoader();
        final Map<String, Set<Switch<?>>> switches = switchLoader.loadSwitches(configStream);

        List<String> urlPatterns = switches.keySet().stream()
                .sorted((p1, p2) -> -1 * Integer.compare(p1.length(), p2.length())).toList();
        for (String urlPattern : urlPatterns) {
            new SwitchRouter(app, taskQueue, urlPattern, switches.get(urlPattern));
            LOGGER.info("Configured a new switch for URL " + urlPattern);
        }
    }
}
