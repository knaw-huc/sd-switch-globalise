package nl.knaw.huc.sdswitch.recipe;

import java.io.InputStream;
import java.util.List;

public interface RecipeData<C> {
    C config();

    String header(String header);

    String pathParam(String key);

    String queryParam(String key);

    List<String> queryParams(String key);

    String formParam(String key);

    List<String> formParams(String key);

    InputStream bodyInputStream();
}
