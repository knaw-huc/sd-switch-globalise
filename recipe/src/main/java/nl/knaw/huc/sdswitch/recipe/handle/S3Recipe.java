// foo bar

package nl.knaw.huc.sdswitch.recipe.handle;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;
import java.util.Set;

public class S3Recipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("prefix"))
            throw new RecipeValidationException("Missing required path parameter 'prefix'");
        if (!pathParams.contains("uuid"))
            throw new RecipeValidationException("Missing required path parameter 'uuid'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        String prefix = data.pathParam("prefix");
        String uuid = data.pathParam("uuid");
        String url = "https://globalise.huygens.knaw.nl/#" + "/" + uuid + "?noredirect";
        return RecipeResponse.withRedirect(url, 301);
    }
}
