module org.knaw.huc.sdswitch.server {
    requires io.javalin;
    requires Saxon.HE;
    requires SaxonUtils;
    requires java.ws.rs;
    requires com.fasterxml.jackson.databind;
    exports org.knaw.huc.sdswitch.server.recipe;
    exports org.knaw.huc.sdswitch.server.config;
    uses org.knaw.huc.sdswitch.server.recipe.Recipe;
}
