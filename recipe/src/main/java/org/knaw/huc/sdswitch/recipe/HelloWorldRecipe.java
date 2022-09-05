package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.util.Set;

public class HelloWorldRecipe implements Recipe<Void> {
    @Override
    public Void parseConfig(XdmItem config, Set<String> pathParams) {
        return null;
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        return RecipeResponse.withBody("Hello " + data.pathParams().get("name"), "text/plain");
    }
}
