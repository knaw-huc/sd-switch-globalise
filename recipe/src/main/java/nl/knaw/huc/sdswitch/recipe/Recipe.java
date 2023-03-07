package nl.knaw.huc.sdswitch.recipe;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public interface Recipe<C> {
    void validateConfig(C config, Set<String> pathParams) throws RecipeValidationException;

    RecipeResponse withData(RecipeData<C> data) throws RecipeException;

    @SuppressWarnings("unchecked")
    static Map<String, Recipe<?>> loadRecipes() {
        return ServiceLoader
                .load(Recipe.class)
                .stream()
                .collect(Collectors.toMap(provider -> provider.type().getName(), ServiceLoader.Provider::get));
    }
}
