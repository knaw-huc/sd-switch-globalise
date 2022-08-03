package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XsltTransformer;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeData;
import org.knaw.huc.sdswitch.server.recipe.RecipeException;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.server.recipe.RecipeResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import mjson.Json;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DreamFactoryRecipe implements Recipe<DreamFactoryRecipe.DreamFactoryConfig> {
    record DreamFactoryConfig(String type, String table, String baseUrl, String accept, String apiKey, String related,
                              Format format, JsonToHtml toHtml, JsonToTtl toTtl) {
        private enum Format {JSON, HTML, TTL}
    }

    @Override
    public DreamFactoryConfig parseConfig(XdmItem config, XdmItem parentConfig, Set<String> pathParams) throws RecipeParseException {
        try {
            String type = Recipe.parse("type", config, parentConfig);
            if (type.isBlank()) {
                throw new RecipeParseException("Missing required type");
            }

            String table = Recipe.parse("table", config, parentConfig);
            if (table.isBlank()) {
                throw new RecipeParseException("Missing required table");
            }

            String baseUrl = Recipe.parse("base-url", config, parentConfig);
            if (baseUrl.isBlank()) {
                throw new RecipeParseException("Missing required base-url");
            }

            String apiKey = Recipe.parse("api-key", config, parentConfig);
            if (apiKey.isBlank()) {
                throw new RecipeParseException("Missing required api-key");
            }

            String related = Recipe.parse("related", config, parentConfig);
            String accept = Recipe.parse("accept", config, parentConfig);

            DreamFactoryConfig.Format format = switch (Recipe.parse("format", config, parentConfig)) {
                case "json" -> DreamFactoryConfig.Format.JSON;
                case "html" -> DreamFactoryConfig.Format.HTML;
                case "ttl" -> DreamFactoryConfig.Format.TTL;
                default -> throw new RecipeParseException("Missing required format (json / html / ttl)");
            };

            String xml2HtmlPath = Recipe.parse("xml2html-path", config, parentConfig);
            XsltTransformer toHtmlTransformer = Saxon.buildTransformer(new File(xml2HtmlPath)).load();
            JsonToHtml toHtml = new JsonToHtml(toHtmlTransformer);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            String ttlSchemaPath = Recipe.parse("ttl-schema-path", config, parentConfig);
            Document ttlSchema = db.parse(new FileInputStream(ttlSchemaPath));
            JsonToTtl toTtl = new JsonToTtl(ttlSchema);

            return new DreamFactoryConfig(type, table, baseUrl, accept, apiKey, related, format, toHtml, toTtl);
        } catch (Exception ex) {
            throw new RecipeParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<DreamFactoryConfig> data) throws RecipeException {
        try {
            String url = String.format("%s/api/v2/%s/_table/%s",
                    data.config().baseUrl(), data.config().type(),
                    URLEncoder.encode(data.config().table(), StandardCharsets.UTF_8.toString()));

            if (!data.context().pathParam("id").isEmpty()) {
                String related = data.config().related();
                if (related == null) {
                    related = "";
                }
                else {
                    related = "&related=" + URLEncoder.encode(related, StandardCharsets.UTF_8.toString());
                }
                // related=* gives 'not implemented'
                url += String.format("/%s?fields=*%s",
                        URLEncoder.encode(data.context().pathParam("id"), StandardCharsets.UTF_8.toString()),
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

            String text = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            Json jsonObject = Json.read(text);
            // Get the references from config
            String related = data.config().related();
            if (related == null) {
                related = "";
            }
            String[] relations = related.split(",");
            for (String relation : relations) {
                fillReference(jsonObject, relation);
            }

            String body = switch (data.config().format()) {
                case JSON -> jsonObject.toString();
                case TTL -> data.config().toTtl().toTtl(jsonObject.toString());
                case HTML -> data.config().toHtml().toHtml(jsonObject.toString());
            };

            String contentType = switch (data.config().format()) {
                case JSON -> "text/json";
                case TTL -> "text/ttl";
                case HTML -> "text/html";
            };

            return RecipeResponse.withBody(body, contentType);
        } catch (IOException | SaxonApiException ex) {
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
}
