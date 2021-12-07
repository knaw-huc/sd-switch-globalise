module org.knaw.huc.sdswitch.recipe {
    requires Saxon.HE;
    requires SaxonUtils;
    requires org.knaw.huc.sdswitch.server;
    provides org.knaw.huc.sdswitch.server.recipe.Recipe
            with org.knaw.huc.sdswitch.recipe.HelloWorldRecipe, org.knaw.huc.sdswitch.recipe.DreamFactoryRecipe;
}
