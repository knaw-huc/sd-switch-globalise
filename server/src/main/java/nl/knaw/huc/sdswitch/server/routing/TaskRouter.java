package nl.knaw.huc.sdswitch.server.routing;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.JavalinValidation;
import nl.knaw.huc.sdswitch.server.queue.TaskQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import static nl.knaw.huc.sdswitch.server.util.Server.DOMAIN;

public class TaskRouter {
    private final TaskQueue taskQueue;

    static {
        JavalinValidation.register(UUID.class, UUID::fromString);
    }

    public TaskRouter(Javalin app, TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
        app.get("/task/{uuid}/status", this::withTaskStatusRequest);
        app.get("/task/{uuid}", this::withTaskResultRequest);

    }

    private void withTaskStatusRequest(Context ctx) {
        UUID uuid = ctx.pathParamAsClass("uuid", UUID.class).get();
        if (taskQueue.getTask(uuid) == null) {
            ctx.status(HttpStatus.NOT_FOUND);
        } else if (taskQueue.getTask(uuid).isDone()) {
            ctx.redirect(DOMAIN + "/task/" + uuid, HttpStatus.FOUND);
        } else {
            ctx.status(HttpStatus.OK);
        }
    }

    private void withTaskResultRequest(Context ctx) throws IOException {
        UUID uuid = ctx.pathParamAsClass("uuid", UUID.class).get();
        if (taskQueue.getTask(uuid) == null || !taskQueue.getTask(uuid).isDone()) {
            ctx.status(HttpStatus.NOT_FOUND);
        } else {
            ctx.contentType(taskQueue.getContentType(uuid));
            ctx.result(Files.readAllBytes(taskQueue.getTaskResult(uuid)));
        }
    }
}
