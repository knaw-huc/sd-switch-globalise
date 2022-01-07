module org.knaw.huc.sdswitch.server {
    requires java.xml;
    requires io.javalin;
    requires Saxon.HE;
    requires SaxonUtils;
    requires mjson;
    exports org.knaw.huc.sdswitch.server.recipe;
    uses org.knaw.huc.sdswitch.server.recipe.Recipe;
}
