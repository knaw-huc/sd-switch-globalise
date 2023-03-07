package nl.knaw.huc.sdswitch.recipe.helloworld;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;

import java.util.Set;

public class HelloWorldRecipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("name"))
            throw new RecipeValidationException("Missing required path parameter 'name'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        return RecipeResponse.withBody("Hello " + data.pathParam("name"), "text/plain");
    }
}
