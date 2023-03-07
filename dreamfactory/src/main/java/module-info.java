module nl.knaw.huc.sdswitch.dreamfactory {
    requires java.xml;
    requires mjson;
    requires Saxon.HE;
    requires SaxonUtils;
    requires nl.knaw.huc.sdswitch.recipe;
    provides nl.knaw.huc.sdswitch.recipe.Recipe with nl.knaw.huc.sdswitch.dreamfactory.DreamFactoryRecipe;
    opens nl.knaw.huc.sdswitch.dreamfactory to com.fasterxml.jackson.databind;
}
