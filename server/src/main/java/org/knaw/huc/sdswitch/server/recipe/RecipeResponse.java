package org.knaw.huc.sdswitch.server.recipe;

import java.io.InputStream;

public record RecipeResponse(InputStream inputStream, String contentType, String redirect) {
    public static RecipeResponse withBody(InputStream inputStream, String contentType) {
        return new RecipeResponse(inputStream, contentType, null);
    }

    public static RecipeResponse withRedirect(String url) {
        return new RecipeResponse(null, null, url);
    }
}
