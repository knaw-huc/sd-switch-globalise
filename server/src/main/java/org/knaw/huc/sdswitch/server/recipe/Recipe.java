package org.knaw.huc.sdswitch.server.recipe;

import java.util.Map;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.toMap;

public interface Recipe {
    RecipeResponse withData(RecipeData data) throws RecipeException;

    static Map<String, Recipe> getRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }
}
