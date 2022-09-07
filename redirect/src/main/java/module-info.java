module org.knaw.huc.sdswitch.redirect {
    requires Saxon.HE;
    requires SaxonUtils;
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.redirect.RedirectRecipe;
}
