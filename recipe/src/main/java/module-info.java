module org.knaw.huc.sdswitch.recipe {
    requires org.knaw.huc.sdswitch.server;
    provides org.knaw.huc.sdswitch.server.recipe.Recipe
            with org.knaw.huc.sdswitch.recipe.HelloWorldRecipe;
}
