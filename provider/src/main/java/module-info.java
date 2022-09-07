module org.knaw.huc.sdswitch.provider {
    requires Saxon.HE;
    requires SaxonUtils;
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.provider.ProviderRecipe;
}
