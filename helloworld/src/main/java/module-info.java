module org.knaw.huc.sdswitch.helloworld {
    requires Saxon.HE;
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.helloworld.HelloWorldRecipe;
}
