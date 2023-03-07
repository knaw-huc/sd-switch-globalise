module nl.knaw.huc.sdswitch.server {
    requires java.xml;
    requires io.javalin;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires nl.knaw.huc.sdswitch.recipe;
    opens nl.knaw.huc.sdswitch.server.config to com.fasterxml.jackson.databind;
}
