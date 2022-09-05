module org.knaw.huc.sdswitch.dreamfactory {
    requires java.xml;
    requires Saxon.HE;
    requires SaxonUtils;
    requires mjson;
    requires org.knaw.huc.sdswitch.recipe;
    provides org.knaw.huc.sdswitch.recipe.Recipe with org.knaw.huc.sdswitch.dreamfactory.DreamFactoryRecipe;
}
