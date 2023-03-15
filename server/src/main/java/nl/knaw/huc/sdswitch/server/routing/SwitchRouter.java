package nl.knaw.huc.sdswitch.server.routing;

import io.javalin.Javalin;
import io.javalin.http.Context;
import nl.knaw.huc.sdswitch.server.queue.TaskQueue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class SwitchRouter {
    private final TaskQueue taskQueue;
    private final Set<Switch<?>> switches;

    public SwitchRouter(Javalin app, TaskQueue taskQueue, String urlPattern, Set<Switch<?>> switches) {
        this.taskQueue = taskQueue;
        this.switches = switches;
        app.get(urlPattern, this::withRequest);
        app.post(urlPattern, this::withRequest);
    }

    private void withRequest(Context ctx) {
        Switch<?> matchingSwitch = null;
        if (switches.size() > 1) {
            String accept = ctx.header("accept");
            if (accept != null) {
                Optional<? extends Switch<?>> switchMatchingAccept = Arrays.stream(accept.split(","))
                        .map(MimeTypeQFactor::create)
                        .sorted()
                        .map(mime -> switches.stream().filter(aSwitch ->
                                mime.matches(aSwitch.getAcceptMimeType())).findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                if (switchMatchingAccept.isPresent())
                    matchingSwitch = switchMatchingAccept.get();
            }
        }

        if (matchingSwitch == null) {
            Optional<Switch<?>> firstSwitch = switches.stream().findFirst();
            if (firstSwitch.isPresent())
                matchingSwitch = firstSwitch.get();
            else
                throw new RuntimeException("No configured switch found!");
        }

        matchingSwitch.handle(ctx, taskQueue);
    }
}
