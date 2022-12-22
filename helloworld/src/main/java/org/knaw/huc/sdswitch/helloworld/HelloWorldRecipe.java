package org.knaw.huc.sdswitch.helloworld;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.recipe.Recipe;
import org.knaw.huc.sdswitch.recipe.RecipeData;
import org.knaw.huc.sdswitch.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.recipe.RecipeResponse;

import java.util.Set;

public class HelloWorldRecipe implements Recipe<Void> {
    @Override
    public Void parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        if (!pathParams.contains("name")) {
            throw new RecipeParseException("Missing required path parameter 'name'");
        }
        return null;
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        return RecipeResponse.withBody("Hello " + data.pathParam("name"), "text/plain");
    }
}
