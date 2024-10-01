module nl.knaw.huc.sdswitch.recipe {
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.json;
    exports nl.knaw.huc.sdswitch.recipe;
    uses nl.knaw.huc.sdswitch.recipe.Recipe;
    provides nl.knaw.huc.sdswitch.recipe.Recipe with nl.knaw.huc.sdswitch.recipe.helloworld.HelloWorldRecipe,
        nl.knaw.huc.sdswitch.recipe.handle.HandleRecipe;
}
