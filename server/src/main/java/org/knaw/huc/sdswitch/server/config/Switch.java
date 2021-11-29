package org.knaw.huc.sdswitch.server.config;

import io.javalin.http.Context;
import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.server.recipe.Recipe;

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
        Recipe.RecipeData data = new Recipe.RecipeData(context.pathParamMap(), config);

        String redirect = recipe.redirect(data);
        if (redirect != null) {
            context.redirect(redirect);
            return;
        }

        context.contentType(recipe.contentType(data));
        context.result(recipe.body(data));
    }
}
