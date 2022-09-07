package org.knaw.huc.sdswitch.server.config;

import io.javalin.http.Context;
import org.knaw.huc.sdswitch.recipe.Recipe;
import org.knaw.huc.sdswitch.recipe.RecipeData;
import org.knaw.huc.sdswitch.recipe.RecipeException;
import org.knaw.huc.sdswitch.recipe.RecipeResponse;

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

            if (response != null) {
                context.status(response.statusCode());

                if (response.redirect() != null) {
                    context.redirect(response.redirect());
                    return;
                }

                if (response.contentType() != null) {
                    context.contentType(response.contentType());
                }

                if (response.body() != null) {
                    context.result(response.body());
                    return;
                }

                if (response.byteArray() != null) {
                    context.result(response.byteArray());
                    return;
                }

                if (response.inputStream() != null) {
                    context.result(response.inputStream());
                    return;
                }
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
