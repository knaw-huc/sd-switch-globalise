// foo bar

package nl.knaw.huc.sdswitch.recipe.handle;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

public class S3Recipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("prefix"))
            throw new RecipeValidationException("Missing required path parameter 'prefix'");
        if (!pathParams.contains("uuid"))
            throw new RecipeValidationException("Missing required path parameter 'uuid'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        String prefix = data.pathParam("prefix");
        String uuid = data.pathParam("uuid");
        String url = "https://globalise.huygens.knaw.nl/#" + "/" + uuid + "?noredirect";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String body = response.body();
//        return RecipeResponse.withRedirect(url, 301);
        String contentType = "application/json";
        return RecipeResponse.withBody(body, contentType);
    }
}
