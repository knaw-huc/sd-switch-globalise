module nl.knaw.huc.sdswitch.recipe {
    exports nl.knaw.huc.sdswitch.recipe;
    uses nl.knaw.huc.sdswitch.recipe.Recipe;
    provides nl.knaw.huc.sdswitch.recipe.Recipe with nl.knaw.huc.sdswitch.recipe.helloworld.HelloWorldRecipe;
}
