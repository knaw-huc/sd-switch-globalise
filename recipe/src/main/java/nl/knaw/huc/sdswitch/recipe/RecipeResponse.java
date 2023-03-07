package nl.knaw.huc.sdswitch.recipe;

import java.io.InputStream;

public record RecipeResponse(String body, byte[] byteArray, InputStream inputStream,
                             String contentType, String redirect, int statusCode) {
    public static RecipeResponse withStatus(String message, int statusCode) {
        return new RecipeResponse(message, null, null, null, null, statusCode);
    }

    public static RecipeResponse withBody(String body, String contentType) {
        return new RecipeResponse(body, null, null, contentType, null, 200);
    }

    public static RecipeResponse withBody(byte[] byteArray, String contentType) {
        return new RecipeResponse(null, byteArray, null, contentType, null, 200);
    }

    public static RecipeResponse withBody(InputStream inputStream, String contentType) {
        return new RecipeResponse(null, null, inputStream, contentType, null, 200);
    }

    public static RecipeResponse withRedirect(String url) {
        return new RecipeResponse(null, null, null, null, url, 301);
    }

    public static RecipeResponse withRedirect(String url, int statusCode) {
        return new RecipeResponse(null, null, null, null, url, statusCode);
    }
}
