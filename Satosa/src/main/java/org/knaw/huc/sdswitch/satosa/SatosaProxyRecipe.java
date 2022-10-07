package org.knaw.huc.sdswitch.satosa;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.auth.OpenID;
import org.knaw.huc.sdswitch.recipe.Recipe;
import org.knaw.huc.sdswitch.recipe.RecipeData;
import org.knaw.huc.sdswitch.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.recipe.RecipeResponse;

import java.util.Set;
import java.util.UUID;

public class SatosaProxyRecipe implements Recipe<SatosaProxyRecipe.SatosaProxyConfig> {
    record SatosaProxyConfig(OpenID openID) {
    }

    @Override
    public SatosaProxyConfig parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        return null;
    }

    @Override
    public RecipeResponse withData(RecipeData<SatosaProxyConfig> data) {
        // Get auth key
        String auth = null;
        if (data.headers().containsKey("Authorization"))
            auth = data.headers().get("Authorization").replaceFirst("^Basic:", "").trim();

        // Start login flow
        if (auth == null) {
            String state = UUID.randomUUID().toString();
            return RecipeResponse.withRedirect(data.config().openID().createAuthUri(state).toString());
        }


    }
}
