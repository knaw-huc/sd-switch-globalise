package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;

import java.io.InputStream;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.toMap;

public interface Recipe {
    default String redirect(RecipeData data) {
        return null;
    }

    default InputStream body(RecipeData data) {
        return null;
    }

    default String contentType(RecipeData data) {
        return null;
    }

    static Map<String, Recipe> getRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }

    final record RecipeData(Map<String, String> pathParams, XdmItem config) {
    }
}
