package nl.knaw.huc.sdswitch.server.routing;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.server.queue.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nl.knaw.huc.sdswitch.server.util.Server.DOMAIN;

public class TaskRouter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Switch.class);

    private final TaskQueue taskQueue;

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

    private void withTaskResultRequest(Context ctx) {
        UUID uuid = ctx.pathParamAsClass("uuid", UUID.class).get();
        Future<RecipeException> taskFuture = taskQueue.getTask(uuid);

        try {
            if (taskFuture == null || !taskFuture.isDone()) {
                ctx.status(HttpStatus.NOT_FOUND);
            } else if (taskFuture.isCancelled()) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.result("Task was cancelled!");
                LOGGER.error("No task result as task " + uuid + " was cancelled!");
            } else {
                RecipeException ex = taskFuture.get();

                if (ex != null) {
                    ctx.status(ex.getHttpStatus());
                    ctx.result(ex.isInternalServerError() ? "Internal Server Error" : ex.getMessage());

                    if (ex.isInternalServerError()) {
                        LOGGER.error("No task result as task " + uuid + " failed: " + ex.getMessage(), ex);
                    }
                } else {
                    ctx.contentType(taskQueue.getContentType(uuid));
                    ctx.result(Files.readAllBytes(taskQueue.getTaskResult(uuid)));
                }
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("Internal Server Error");
            LOGGER.error("No task result as task " + uuid + " failed: " + e.getMessage(), e);
        }
    }
}
