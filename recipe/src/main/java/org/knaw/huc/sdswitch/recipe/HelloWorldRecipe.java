package org.knaw.huc.sdswitch.recipe;

import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class HelloWorldRecipe implements Recipe {
    public RecipeResponse withData(RecipeData data) {
        return RecipeResponse.withBody(
                new ByteArrayInputStream(("Hello " + data.pathParams().get("name")).getBytes(StandardCharsets.UTF_8)),
                "text/plain");
    }
}
