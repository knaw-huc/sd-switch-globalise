module nl.knaw.huc.sdswitch.data {
    requires nl.knaw.huc.sdswitch.recipe;
    provides nl.knaw.huc.sdswitch.recipe.Recipe with
            nl.knaw.huc.sdswitch.data.ProviderRecipe,
            nl.knaw.huc.sdswitch.data.RedirectRecipe;
    opens nl.knaw.huc.sdswitch.data to com.fasterxml.jackson.databind;
    exports nl.knaw.huc.sdswitch.data;
}
