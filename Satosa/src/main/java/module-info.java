module org.knaw.huc.sdswitch.satosa {
    requires Saxon.HE;
    requires SaxonUtils;
    requires java.ws.rs;
    requires org.knaw.huc.sdswitch.recipe;
    requires org.knaw.huc.auth;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.satosa.SatosaProxyRecipe;
}
