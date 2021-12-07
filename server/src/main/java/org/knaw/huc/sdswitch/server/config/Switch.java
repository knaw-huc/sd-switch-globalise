package org.knaw.huc.sdswitch.server.config;

import io.javalin.http.Context;
import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

public class Switch {
    private final Recipe recipe;
    private final String urlPattern;
    private final XdmItem config;

    public Switch(Recipe recipe, String urlPattern, XdmItem config) {
        this.recipe = recipe;
        this.urlPattern = urlPattern;
        this.config = config;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void handle(Context context) {
        try {
            RecipeData data = new RecipeData(context.pathParamMap(), config);
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
}
