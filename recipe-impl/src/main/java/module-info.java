module org.knaw.huc.sdswitch.impl {
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe
            with org.knaw.huc.sdswitch.impl.HelloWorldRecipe;
}
