package org.knaw.huc.sdswitch.redirect;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.recipe.*;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectRecipe implements Recipe<RedirectRecipe.RedirectRecipeConfig> {
    record RedirectRecipeConfig(Pattern pattern, String url, boolean isTempRedirect) {
    }

    @Override
    public RedirectRecipeConfig parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        try {
            if (!pathParams.contains("path")) {
                throw new RecipeParseException("Missing path parameter 'path'");
            }

            XdmValue redirectValue = Saxon.xpath(config, "redirect");
            if (redirectValue.size() == 0) {
                throw new RecipeParseException("Missing redirect");
            }

            XdmItem redirect = redirectValue.itemAt(0);

            String pattern = Saxon.xpath2string(redirect, "@pattern").trim();
            if (pattern.isBlank()) {
                throw new RecipeParseException("Missing required pattern");
            }

            String url = redirect.getStringValue().trim();
            if (url.isBlank()) {
                throw new RecipeParseException("Missing required redirect url");
            }

            return new RedirectRecipeConfig(Pattern.compile(pattern), url,
                    Saxon.hasAttribute(config, "is-temp-redirect"));
        } catch (Exception ex) {
            throw new RecipeParseException(ex.getMessage(), ex);
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<RedirectRecipeConfig> data) {
        Matcher matcher = data.config().pattern().matcher(data.pathParam("path"));
        if (matcher.matches()) {
            String redirectTo = matcher.replaceFirst(data.config().url());
            return RecipeResponse.withRedirect(redirectTo, data.config().isTempRedirect() ? 302 : 301);
        }
        return RecipeResponse.withStatus("Not Found", 404);
    }
}
