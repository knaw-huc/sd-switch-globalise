package nl.knaw.huc.sdswitch.recipe.handle;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import java.util.Set;

public class HandleRecipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("prefix"))
            throw new RecipeValidationException("Missing required path parameter 'prefix'");
        if (!pathParams.contains("suffix"))
            throw new RecipeValidationException("Missing required path parameter 'suffix'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        // work to be done
        // Matcher matcher = data.config().pattern().matcher(data.pathParam("path"));
        // if (matcher.matches()) {
        //     String redirectTo = matcher.replaceFirst(data.config().redirectTo());
        //     return RecipeResponse.withRedirect(redirectTo, data.config().isTempRedirect() ? 302 : 301);
        // }
        // return RecipeResponse.withStatus("Not Found", 404);
        //     return RecipeResponse.withRedirect(redirectTo, data.config().isTempRedirect() ? 302 : 301);
        return RecipeResponse.withBody("Handle: " + data.pathParam("name"), "text/plain");
    }

}
