package org.knaw.huc.sdswitch.server.config;

import io.javalin.http.Context;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.util.regex.Pattern;

public class Switch<C> {
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

    public void handle(Context context) {
        try {
            RecipeData<C> data = new RecipeData<>(context.pathParamMap(), config);
            RecipeResponse response = recipe.withData(data);

            if (response != null && response.redirect() != null) {
                context.redirect(response.redirect());
                return;
            }

            if (response != null && response.contentType() != null && response.inputStream() != null) {
                context.contentType(response.contentType());
                context.result(response.inputStream());
                return;
            }

            throw new RecipeException("No data from recipe!");
        } catch (RecipeException ex) {
            context.status(ex.getHttpStatus());
            context.result(ex.getHttpStatus() != 500 ? ex.getMessage() : "Internal Server Error");
        }
    }

    public static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern, String acceptMimeType, C config) {
        return new Switch<>(recipe, urlPattern, acceptMimeType, config);
    }
}
