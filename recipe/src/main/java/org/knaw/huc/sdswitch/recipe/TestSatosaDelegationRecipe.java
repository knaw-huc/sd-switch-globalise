package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;
import org.knaw.huc.sdswitch.server.security.data.User;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Set;

public class TestSatosaDelegationRecipe implements Recipe<URI> {
    @Override
    public URI parseConfig(XdmItem config, XdmItem parentConfig, Set<String> pathParams) throws RecipeParseException {
        try {
            String remote = Recipe.parse("remote", config, parentConfig);
            if (remote.isBlank()) {
                throw new RecipeParseException("Missing required remote");
            }
            return URI.create(remote);
        } catch (Exception ex) {
            throw new RecipeParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<URI> data) throws RecipeException {
        try {
            if (data.context().attribute("user") instanceof User user) {
                HttpURLConnection conn = (HttpURLConnection) data.config().toURL().openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("authorization", "Bearer " + user.getTokens().accessToken());

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                    return RecipeResponse.withBody(conn.getInputStream(), "text/plain");
            }

            throw new RecipeException("You are an anonymous user", 400);
        } catch (Exception e) {
            throw new RecipeException(e.getMessage(), e);
        }
    }
}
