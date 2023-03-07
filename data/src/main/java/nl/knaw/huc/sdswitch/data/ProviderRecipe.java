package nl.knaw.huc.sdswitch.data;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeException;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;
import nl.knaw.huc.sdswitch.recipe.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProviderRecipe implements Recipe<ProviderRecipe.ProviderRecipeConfig> {
    record ProviderRecipeConfig(String provide, Pattern pattern, String contentType) {
    }

    @Override
    public void validateConfig(ProviderRecipe.ProviderRecipeConfig config, Set<String> pathParams)
            throws RecipeValidationException {
        if (config.pattern != null && !pathParams.contains("path"))
            throw new RecipeValidationException("Provide has a 'pattern', but is missing a path parameter 'path'");

        if (config.pattern == null && pathParams.contains("path"))
            throw new RecipeValidationException("Provide has no 'pattern', but there is a path parameter 'path'");

        if (config.provide == null)
            throw new RecipeValidationException("Missing required provide path");

        if (config.contentType == null)
            throw new RecipeValidationException("Missing required 'contentType'");
    }

    @Override
    public RecipeResponse withData(RecipeData<ProviderRecipeConfig> data) throws RecipeException {
        try {
            Path path;
            if (data.pathParam("path") != null) {
                Matcher matcher = data.config().pattern().matcher(data.pathParam("path"));
                if (!matcher.matches())
                    return RecipeResponse.withStatus("Not Found", 404);

                String matchingPath = matcher.replaceFirst(data.config().provide());
                path = Paths.get(matchingPath);
            } else
                path = Paths.get(data.config().provide());

            if (!Files.exists(path))
                return RecipeResponse.withStatus("Not Found", 404);

            return RecipeResponse.withBody(Files.readAllBytes(path), data.config().contentType());
        } catch (IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }
}
