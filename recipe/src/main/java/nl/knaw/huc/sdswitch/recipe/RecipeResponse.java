package nl.knaw.huc.sdswitch.recipe;

import java.io.InputStream;

public sealed interface RecipeResponse permits
        RecipeResponse.StringResponse, RecipeResponse.BytesResponse, RecipeResponse.StreamResponse,
        RecipeResponse.RedirectResponse, RecipeResponse.AsyncTaskResponse {
    static RecipeResponse withStatus(String message, int statusCode) {
        return new StringResponse(message, null, statusCode);
    }

    static RecipeResponse withBody(String body, String contentType) {
        return new StringResponse(body, contentType, 200);
    }

    static RecipeResponse withBody(byte[] byteArray, String contentType) {
        return new BytesResponse(byteArray, contentType, 200);
    }

    static RecipeResponse withBody(InputStream inputStream, String contentType) {
        return new StreamResponse(inputStream, contentType, 200);
    }

    static RecipeResponse withRedirect(String url) {
        return new RedirectResponse(url, 301);
    }

    static RecipeResponse withRedirect(String url, int statusCode) {
        return new RedirectResponse(url, statusCode);
    }

    static RecipeResponse withTask(RecipeTask task, String contentType) {
        return new AsyncTaskResponse(task, contentType);
    }

    record StringResponse(String body, String contentType, int statusCode) implements RecipeResponse {
    }

    record BytesResponse(byte[] body, String contentType, int statusCode) implements RecipeResponse {
    }

    record StreamResponse(InputStream inputStream, String contentType, int statusCode) implements RecipeResponse {
    }

    record RedirectResponse(String url, int statusCode) implements RecipeResponse {
    }

    record AsyncTaskResponse(RecipeTask task, String contentType) implements RecipeResponse {
    }
}
