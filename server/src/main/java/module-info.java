module org.knaw.huc.sdswitch.server {
    requires org.slf4j;
    requires io.javalin;
    requires Saxon.HE;
    requires SaxonUtils;
    requires java.ws.rs;
    requires com.fasterxml.jackson.databind;
    requires org.knaw.huc.auth;
    exports org.knaw.huc.sdswitch.server.recipe;
    exports org.knaw.huc.sdswitch.server.config;
    exports org.knaw.huc.sdswitch.server.security.data;
    uses org.knaw.huc.sdswitch.server.recipe.Recipe;
}
