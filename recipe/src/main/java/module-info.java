module org.knaw.huc.sdswitch.recipe {
    requires Saxon.HE;
    requires SaxonUtils;
    requires org.knaw.huc.auth;
    requires org.knaw.huc.sdswitch.server;
    requires mjson;
    requires java.xml;
    provides org.knaw.huc.sdswitch.server.recipe.Recipe with
            org.knaw.huc.sdswitch.recipe.HelloWorldRecipe,
            org.knaw.huc.sdswitch.recipe.DreamFactoryRecipe,
            org.knaw.huc.sdswitch.recipe.TestSatosaRecipe,
            org.knaw.huc.sdswitch.recipe.TestSatosaDelegationRecipe;
}
