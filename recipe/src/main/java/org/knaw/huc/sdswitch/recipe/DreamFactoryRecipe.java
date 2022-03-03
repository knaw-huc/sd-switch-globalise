package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import mjson.Json;

public class DreamFactoryRecipe implements Recipe<DreamFactoryRecipe.DreamFactoryConfig> {
    private String jsonOrTtl = "json";

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

            if (Saxon.xpath2string(config, "json-or-ttl")!=null) {
                jsonOrTtl = Saxon.xpath2string(config, "json-or-ttl");
            }

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
            if (related == null) {
              related = "";
            } else {
              related = "&related=" + URLEncoder.encode(related, StandardCharsets.UTF_8.toString());
            }
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

          if (conn.getResponseCode() != 200) {
            throw new RecipeException(conn.getResponseMessage(), conn.getResponseCode());
          }

          String text = new BufferedReader(
              new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
              .lines()
              .collect(Collectors.joining("\n"));

          Json jsonObject = Json.read(text);
          // Get the reference from config
          String reference = "academische_titel_by_academischetitel_id";
          fillReference(jsonObject, reference);
          reference = "adellijke_titel_by_adellijketitel_id";
          fillReference(jsonObject, reference);

          InputStream is = null;
          if (jsonOrTtl.equals("ttl")) {
            String ttlString = JsonToTtl.jsonToTtl(jsonObject.toString());
            is = new ByteArrayInputStream(ttlString.getBytes());
          }
          if (jsonOrTtl.equals("json")) {
            is = new ByteArrayInputStream(jsonObject.toString().getBytes());
          }
          return RecipeResponse.withBody(is, conn.getHeaderField("Content-Type"));
        } catch (IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }

  static void fillReference(Json jsonObject, String reference) {
    String[] toFrom = new String[2];
    try {
      Json referedObject = jsonObject.atDel(reference);
      toFrom = reference.split("_by_");
      jsonObject.atDel(toFrom[1]);
      jsonObject.set(toFrom[0], referedObject.at("naam").getValue());
    } catch (UnsupportedOperationException | NullPointerException ex) {
      // other empty fields contain null, so:
      jsonObject.set(toFrom[0], null);
    }
  }

  public void setTtl() {
        jsonOrTtl = "ttl";
    }

    public void setJson() {
        jsonOrTtl = "json";
    }
}
