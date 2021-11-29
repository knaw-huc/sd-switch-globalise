package org.knaw.huc.sdswitch.recipe;

import org.knaw.huc.sdswitch.server.recipe.Recipe;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HelloWorldRecipe implements Recipe {
    public InputStream body(RecipeData data) {
        return new ByteArrayInputStream(("Hello " + data.pathParams().get("name")).getBytes(StandardCharsets.UTF_8));
    }

    public String contentType(RecipeData data) {
        return "text/plain";
    }
}
