package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class HelloWorldRecipe implements Recipe<Void> {
    @Override
    public Set<String> requiredPathParams() {
        return Set.of("name");
    }

    @Override
    public Void parseConfig(XdmItem config, XdmItem parentConfig) {
        return null;
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        return RecipeResponse.withBody(
                new ByteArrayInputStream(("Hello " + data.pathParams().get("name")).getBytes(StandardCharsets.UTF_8)),
                "text/plain");
    }
}
