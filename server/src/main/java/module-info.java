module nl.knaw.huc.sdswitch.server {
    requires java.xml;
    requires io.javalin;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.module.paramnames;
    requires nl.knaw.huc.sdswitch.recipe;
    requires kotlin.stdlib;
    opens nl.knaw.huc.sdswitch.server.config to com.fasterxml.jackson.databind;
    opens nl.knaw.huc.sdswitch.server.routing to com.fasterxml.jackson.databind;
}
