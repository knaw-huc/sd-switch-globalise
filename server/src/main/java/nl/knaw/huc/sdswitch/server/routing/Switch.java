package nl.knaw.huc.sdswitch.server.routing;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.server.config.RecipeDataImpl;
import nl.knaw.huc.sdswitch.server.queue.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static nl.knaw.huc.sdswitch.server.util.Server.DOMAIN;

public class Switch<C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Switch.class);

    private final Recipe<C> recipe;
    private final String urlPattern;
    private final String acceptMimeType;
    private final C config;

    public Switch(Recipe<C> recipe, String urlPattern, String acceptMimeType, C config) {
        this.recipe = recipe;
        this.urlPattern = urlPattern;
        this.acceptMimeType = acceptMimeType;
        this.config = config;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public String getAcceptMimeType() {
        return acceptMimeType;
    }

    public void handle(Context context, TaskQueue taskQueue) {
        try {
            RecipeData<C> data = new RecipeDataImpl<>(config, context);
            RecipeResponse response = recipe.withData(data);

            if (response != null) {
                if (response instanceof RecipeResponse.StringResponse stringResponse) {
                    if (stringResponse.contentType() != null) {
                        context.contentType(stringResponse.contentType());
                    }
                    context.status(stringResponse.statusCode());
                    context.result(stringResponse.body());
                } else if (response instanceof RecipeResponse.BytesResponse bytesResponse) {
                    if (bytesResponse.contentType() != null) {
                        context.contentType(bytesResponse.contentType());
                    }
                    context.status(bytesResponse.statusCode());
                    context.result(bytesResponse.body());
                } else if (response instanceof RecipeResponse.StreamResponse streamResponse) {
                    if (streamResponse.contentType() != null) {
                        context.contentType(streamResponse.contentType());
                    }
                    context.status(streamResponse.statusCode());
                    context.result(streamResponse.inputStream());
                } else if (response instanceof RecipeResponse.RedirectResponse redirectResponse) {
                    context.redirect(redirectResponse.url());
                    context.status(redirectResponse.statusCode());
                } else if (response instanceof RecipeResponse.AsyncTaskResponse taskResponse) {
                    UUID uuid = taskQueue.addTask(taskResponse.task(), taskResponse.contentType());
                    context.status(HttpStatus.ACCEPTED);
                    context.header("Location", DOMAIN + "/task/" + uuid + "/status");
                } else {
                    throw new RecipeException("No data from recipe!");
                }
            }
        } catch (RecipeException ex) {
            context.status(ex.getHttpStatus());
            context.result(ex.isInternalServerError() ? "Internal Server Error" : ex.getMessage());

            if (ex.isInternalServerError()) {
                LOGGER.error("Request handling failed: " + ex.getMessage(), ex);
            }
        }
    }

    public static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern, String acceptMimeType, C config) {
        return new Switch<>(recipe, urlPattern, acceptMimeType, config);
    }
}
