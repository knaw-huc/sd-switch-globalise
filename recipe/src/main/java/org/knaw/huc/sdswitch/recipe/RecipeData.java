package org.knaw.huc.sdswitch.recipe;

import java.util.Map;

public record RecipeData<C>(Map<String, String> pathParams, C config) {
}
