package nl.knaw.huc.sdswitch.recipe;

public interface ConfigMappingRecipe<C, M> extends Recipe<C> {
    C getConfig(M mapped) throws RecipeMappingException;
}
