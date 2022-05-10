package org.knaw.huc.sdswitch.server.recipe;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import nl.mpi.tla.util.Saxon;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public interface Recipe<C> {
    C parseConfig(XdmItem config, XdmItem parentConfig, Set<String> pathParams) throws RecipeParseException;

    RecipeResponse withData(RecipeData<C> data) throws RecipeException;

    static String parse(String xpath, XdmItem config, XdmItem parentConfig) throws SaxonApiException {
        String result = Saxon.xpath2string(config, xpath);
        if (result.isBlank()) {
            return Saxon.xpath2string(parentConfig, xpath);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Recipe<?>> getRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }
}
