module org.knaw.huc.sdswitch.satosa {
    requires Saxon.HE;
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.satosa.SatosaProxyRecipe;
}
