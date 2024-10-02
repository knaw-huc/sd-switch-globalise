package nl.knaw.huc.sdswitch.recipe.handle;

import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeData;
import nl.knaw.huc.sdswitch.recipe.RecipeResponse;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

public class HandleRecipe implements Recipe<Void> {
    @Override
    public void validateConfig(Void config, Set<String> pathParams) throws RecipeValidationException {
        if (!pathParams.contains("prefix"))
            throw new RecipeValidationException("Missing required path parameter 'prefix'");
        if (!pathParams.contains("suffix"))
            throw new RecipeValidationException("Missing required path parameter 'suffix'");
    }

    @Override
    public RecipeResponse withData(RecipeData<Void> data) {
        String prefix = data.pathParam("prefix");
        String suffix = data.pathParam("suffix");
        String findUrl = "https://hdl.handle.net/api/handles/" + prefix + "/" + suffix + "?noredirect";
        // met een HttpClient doe een GET op https://hdl.handle.net/api/handles/<prefix>/<suffix>?noredirect
        JSONObject json = null;
        JSONArray jArray = null;
        JSONObject jAitem = null;
        String url;
        try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault()) {
            httpclient.start();
            SimpleHttpRequest request = SimpleRequestBuilder.get(findUrl).build();
            Future<SimpleHttpResponse> future = httpclient.execute(request, null);
            // and wait until response is received
            SimpleHttpResponse response = future.get();
            // je krijgt json terug
            // parseer die JSONObject json = JSONObject.fromObject(jsonString);
            // haal hier de URL uit zie screenshot in slack
            // doe een redirect naar de URL RecipeResponse.withRedirect(URL, 301);
            String jsonString = response.getBodyText();
            json = new JSONObject(jsonString);
            jArray = (JSONArray) json.get("values");
            jAitem = ((JSONObject) jArray.get(0)).getJSONObject("data");
            url = jAitem.get("value").toString();
            return RecipeResponse.withRedirect(url, 301);
        } catch (IOException | InterruptedException | ExecutionException e) {
            return RecipeResponse.withStatus("Not Found", 404);
            // throw new RuntimeException(e);
        } catch (JSONException e) {
            if(jArray==null)
            { jArray = new JSONArray( "[NULL]"); }
            if(jAitem==null)
            { jAitem = new JSONObject("{1: --NULL--}"); }
            return RecipeResponse.withBody("<html><body><p>Not Found</p><p>"
                + jArray.get(0) + "</p>\n<p>" + jAitem + "</p></body></html>", "html");
        }
    }
}

