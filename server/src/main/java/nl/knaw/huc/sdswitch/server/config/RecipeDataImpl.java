package nl.knaw.huc.sdswitch.server.config;

import io.javalin.http.Context;
import nl.knaw.huc.sdswitch.recipe.RecipeData;

import java.io.InputStream;
import java.util.List;

public class RecipeDataImpl<C> implements RecipeData<C> {
    private final C config;
    private final Context context;

    public RecipeDataImpl(C config, Context context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public C config() {
        return config;
    }

    @Override
    public String header(String header) {
        return context.header(header);
    }

    @Override
    public String pathParam(String key) {
        try {
            return context.pathParam(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String queryParam(String key) {
        return context.queryParam(key);
    }

    @Override
    public List<String> queryParams(String key) {
        return context.queryParams(key);
    }

    @Override
    public String formParam(String key) {
        return context.formParam(key);
    }

    @Override
    public List<String> formParams(String key) {
        return context.formParams(key);
    }

    @Override
    public InputStream bodyInputStream() {
        return context.bodyInputStream();
    }
}
