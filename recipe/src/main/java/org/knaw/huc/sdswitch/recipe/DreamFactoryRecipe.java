package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DreamFactoryRecipe implements Recipe {
    public RecipeResponse withData(RecipeData data) throws RecipeException {
        try {
            String baseUrl = Saxon.xpath2string(data.config(), "base-url");
            String accept = Saxon.xpath2string(data.config(), "accept");
            String apiKey = Saxon.xpath2string(data.config(), "api-key");

            if (baseUrl == null || apiKey == null)
                throw new RecipeException("Missing required configuration for DreamFactoryRecipe: base-url or api-key");

            String table = data.pathParams().get("table");
            if (table == null)
                throw new RecipeException("Missing required path param for DreamFactoryRecipe: table");

            URL url = new URL(baseUrl + "/" + table);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", accept != null && !accept.isEmpty() ? accept : "application/json");
            conn.setRequestProperty("X-DreamFactory-API-Key", apiKey);

            if (conn.getResponseCode() != 200)
                throw new RecipeException(conn.getResponseMessage(), conn.getResponseCode());

            return RecipeResponse.withBody(conn.getInputStream(), conn.getHeaderField("Content-Type"));
        } catch (SaxonApiException | IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }
}
