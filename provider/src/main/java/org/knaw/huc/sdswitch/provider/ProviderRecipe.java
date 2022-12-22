package org.knaw.huc.sdswitch.provider;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.recipe.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProviderRecipe implements Recipe<ProviderRecipe.ProviderRecipeConfig> {
    record ProviderRecipeConfig(Pattern pattern, String path, String contentType) {
    }

    @Override
    public ProviderRecipeConfig parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        try {
            XdmValue provideNode = Saxon.xpath(config, "provide");
            if (provideNode.size() == 0) {
                throw new RecipeParseException("Missing provide");
            }

            XdmItem provide = provideNode.itemAt(0);

            if (Saxon.hasAttribute(provide, "pattern") && !pathParams.contains("path")) {
                throw new RecipeParseException("Provide has a pattern, but is missing a path parameter 'path'");
            }

            if (!Saxon.hasAttribute(provide, "pattern") && pathParams.contains("path")) {
                throw new RecipeParseException("Provide has no pattern, but there is a path parameter 'path'");
            }

            Pattern pattern = null;
            String patternStr = Saxon.xpath2string(provide, "@pattern").trim();
            if (!patternStr.isBlank()) {
                pattern = Pattern.compile(patternStr);
            }

            String path = provide.getStringValue().trim();
            if (path.isBlank()) {
                throw new RecipeParseException("Missing required provide path");
            }

            String contentType = Saxon.xpath2string(config, "content-type").trim();
            if (contentType.isBlank()) {
                throw new RecipeParseException("Missing required content-type");
            }

            return new ProviderRecipeConfig(pattern, path, contentType);
        } catch (Exception ex) {
            throw new RecipeParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<ProviderRecipeConfig> data) throws RecipeException {
        try {
            Path path;
            if (data.pathParam("path") != null) {
                Matcher matcher = data.config().pattern().matcher(data.pathParam("path"));
                if (!matcher.matches()) {
                    return RecipeResponse.withStatus("Not Found", 404);
                }

                String matchingPath = matcher.replaceFirst(data.config().path());
                path = Paths.get(matchingPath);
            } else {
                path = Paths.get(data.config().path());
            }

            if (!Files.exists(path)) {
                return RecipeResponse.withStatus("Not Found", 404);
            }

            return RecipeResponse.withBody(Files.readAllBytes(path), data.config().contentType());
        } catch (IOException ex) {
            throw new RecipeException(ex.getMessage(), ex);
        }
    }
}
