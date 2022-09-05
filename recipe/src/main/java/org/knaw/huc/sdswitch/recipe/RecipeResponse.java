package org.knaw.huc.sdswitch.recipe;

import java.io.InputStream;

public record RecipeResponse(String body, byte[] byteArray, InputStream inputStream,
                             String contentType, String redirect) {
    public static RecipeResponse withBody(String body, String contentType) {
        return new RecipeResponse(body, null, null, contentType, null);
    }

    public static RecipeResponse withBody(byte[] byteArray, String contentType) {
        return new RecipeResponse(null, byteArray, null, contentType, null);
    }

    public static RecipeResponse withBody(InputStream inputStream, String contentType) {
        return new RecipeResponse(null, null, inputStream, contentType, null);
    }

    public static RecipeResponse withRedirect(String url) {
        return new RecipeResponse(null, null, null, null, url);
    }
}
