package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public interface Recipe<C> {
    C parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException;

    RecipeResponse withData(RecipeData<C> data) throws RecipeException;

    @SuppressWarnings("unchecked")
    static Map<String, Recipe<?>> loadRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(Collectors.toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }
}
