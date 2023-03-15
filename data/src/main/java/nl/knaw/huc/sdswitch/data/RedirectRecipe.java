package nl.knaw.huc.sdswitch.data;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectRecipe implements Recipe<RedirectRecipe.RedirectRecipeConfig> {
    record RedirectRecipeConfig(String redirectTo, Pattern pattern, boolean isTempRedirect) {
    }

    @Override
    public void validateConfig(RedirectRecipeConfig config, Set<String> pathParams)
            throws RecipeValidationException {
        if (!pathParams.contains("path"))
            throw new RecipeValidationException("Missing path parameter 'path'");

        if (config.redirectTo == null)
            throw new RecipeValidationException("Missing required 'redirectTo'");

        if (config.pattern == null)
            throw new RecipeValidationException("Missing required 'pattern'");
    }

    @Override
    public RecipeResponse withData(RecipeData<RedirectRecipeConfig> data) {
        Matcher matcher = data.config().pattern().matcher(data.pathParam("path"));
        if (matcher.matches()) {
            String redirectTo = matcher.replaceFirst(data.config().redirectTo());
            return RecipeResponse.withRedirect(redirectTo, data.config().isTempRedirect() ? 302 : 301);
        }
        return RecipeResponse.withStatus("Not Found", 404);
    }
}
