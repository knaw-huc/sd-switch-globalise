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
    public record DreamFactoryConfig(String type, String baseUrl, String accept,
                                     String apiKey, String related, String jsonOrTtl) {
    }

    @Override
    public Set<String> requiredPathParams() {
        return Set.of("table");
    }

    @Override
    public DreamFactoryConfig parseConfig(XdmItem config, XdmItem parentConfig) throws RecipeParseException {
        try {
            String type = Saxon.xpath2string(config, "type");
            if (type.isBlank()) {
                type = Saxon.xpath2string(parentConfig, "type");
                if (type.isBlank())
                    throw new RecipeParseException("Missing required type");
            }

            String baseUrl = Saxon.xpath2string(config, "base-url");
            if (baseUrl.isBlank()) {
                baseUrl = Saxon.xpath2string(parentConfig, "base-url");
                if (baseUrl.isBlank())
                    throw new RecipeParseException("Missing required base-url");
            }

            String apiKey = Saxon.xpath2string(config, "api-key");
            if (apiKey.isBlank()) {
                apiKey = Saxon.xpath2string(parentConfig, "api-key");
                if (apiKey.isBlank())
                    throw new RecipeParseException("Missing required api-key");
            }

            String related = Saxon.xpath2string(config, "related");
            if (related.isBlank())
                related = Saxon.xpath2string(parentConfig, "related");

            String accept = Saxon.xpath2string(config, "accept");
            if (accept.isBlank())
                accept = Saxon.xpath2string(parentConfig, "accept");

            String jsonOrTtl = Saxon.xpath2string(config, "json-or-ttl");
            if (jsonOrTtl.isBlank()) {
                jsonOrTtl = Saxon.xpath2string(parentConfig, "json-or-ttl");
                if (jsonOrTtl.isBlank())
                    throw new RecipeParseException("Missing required json-or-ttl");
            }

            return new DreamFactoryConfig(type, baseUrl, accept, apiKey, related, jsonOrTtl);
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
                String related = "&related=" +
                        URLEncoder.encode(data.config().related(), StandardCharsets.UTF_8.toString());
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

            String text = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Json jsonObject = Json.read(text);
            try {
                Json acadTitleObj = jsonObject.atDel("academische_titel_by_academischetitel_id");
                jsonObject.atDel("academischetitel_id");
                jsonObject.set("academische_titel", acadTitleObj.at("naam").getValue());
            } catch (UnsupportedOperationException | NullPointerException ex) {
                // academischetitel_id==null
                // other empty fields contain null, so:
                jsonObject.set("academische_titel", null);
            }

            try {
                Json adelsTitleObj = jsonObject.atDel("adellijke_titel_by_adellijketitel_id");
                jsonObject.atDel("adellijketitel_id");
                jsonObject.set("adellijke_titel", adelsTitleObj.at("naam").getValue());
            } catch (UnsupportedOperationException | NullPointerException ex) {
                // adellijketitel_id==null
                // other empty fields contain null, so:
                jsonObject.set("adellijke_titel", null);
            }

            InputStream is = null;
            if (data.config().jsonOrTtl().equals("ttl")) {
                String ttlString = JsonToTtl.jsonToTtl(jsonObject.toString());
                is = new ByteArrayInputStream(ttlString.getBytes());
            }
            if (data.config().jsonOrTtl().equals("json")) {
                is = new ByteArrayInputStream(jsonObject.toString().getBytes());
            }
            return RecipeResponse.withBody(is, conn.getHeaderField("Content-Type"));
        } catch (IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }
}
