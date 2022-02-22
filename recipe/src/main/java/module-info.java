module org.knaw.huc.sdswitch.recipe {
    requires Saxon.HE;
    requires SaxonUtils;
    requires org.knaw.huc.sdswitch.server;
    requires mjson;
    requires java.xml;
    requires com.sun.xml.txw2;
    provides org.knaw.huc.sdswitch.server.recipe.Recipe
            with org.knaw.huc.sdswitch.recipe.HelloWorldRecipe, org.knaw.huc.sdswitch.recipe.DreamFactoryRecipe;
}
