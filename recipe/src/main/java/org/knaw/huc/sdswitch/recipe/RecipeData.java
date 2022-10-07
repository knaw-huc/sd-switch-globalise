package org.knaw.huc.sdswitch.recipe;

import java.util.List;
import java.util.Map;

public record RecipeData<C>(Map<String, String> pathParams,
                            Map<String, List<String>> queryParams,
                            Map<String, String> headers,
                            C config) {
}
