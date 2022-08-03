package org.knaw.huc.sdswitch.server.recipe;

import io.javalin.http.Context;

public record RecipeData<C>(Context context, C config) {
}
