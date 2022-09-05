package org.knaw.huc.sdswitch.server.recipe;

import net.sf.saxon.s9api.XdmItem;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

public interface Recipe<C> {
    C parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException;

    RecipeResponse withData(RecipeData<C> data) throws RecipeException;

    @SuppressWarnings("unchecked")
    static Map<String, Recipe<?>> getRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }
}
