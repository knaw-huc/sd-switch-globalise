package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class DreamFactoryRecipe implements Recipe<DreamFactoryRecipe.DreamFactoryConfig> {
    public record DreamFactoryConfig(String type, String baseUrl, String accept, String apiKey, String related) {
    }

    @Override
    public Set<String> requiredPathParams() {
        return Set.of("table");
    }

    @Override
    public DreamFactoryConfig parseConfig(XdmItem config) throws RecipeParseException {
        try {
            String type = Saxon.xpath2string(config, "type");
            if (type == null)
                throw new RecipeParseException("Missing required type");

            String baseUrl = Saxon.xpath2string(config, "base-url");
            if (baseUrl == null)
                throw new RecipeParseException("Missing required base-url");

            String apiKey = Saxon.xpath2string(config, "api-key");
            if (apiKey == null)
                throw new RecipeParseException("Missing required api-key");

            String related = Saxon.xpath2string(config, "related");

            String accept = Saxon.xpath2string(config, "accept");

            return new DreamFactoryConfig(type, baseUrl, accept, apiKey, related);
        } catch (SaxonApiException ex) {
            throw new RecipeParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<DreamFactoryConfig> data) throws RecipeException {
        try {
            String url = String.format("%s/api/v2/%s/_table/%s",
                    data.config().baseUrl(), data.config().type(),
                    URLEncoder.encode(data.pathParams().get("table"), StandardCharsets.UTF_8.toString()));

            if (data.pathParams().get("id") != null) {
                String related = data.config().related();
                if (related == null)
                    related="";
                else
                    related="&related=" + URLEncoder.encode(related, StandardCharsets.UTF_8.toString());
                // related=* gives 'not implemented'
                url += String.format("/%s?fields=*%s",
                        URLEncoder.encode(data.pathParams().get("id"), StandardCharsets.UTF_8.toString()),
                        related);
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", data.config().accept() != null && !data.config().accept().isEmpty()
                    ? data.config().accept() : "application/json");
            conn.setRequestProperty("X-DreamFactory-API-Key", data.config().apiKey());

            if (conn.getResponseCode() != 200)
                throw new RecipeException(conn.getResponseMessage(), conn.getResponseCode());
            return RecipeResponse.withBody(conn.getInputStream(), conn.getHeaderField("Content-Type"));
        } catch (IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }
}
