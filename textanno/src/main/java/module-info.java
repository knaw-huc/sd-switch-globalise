module org.knaw.huc.sdswitch.textanno {
    requires rdf4j.model;
    requires rdf4j.model.api;
    requires rdf4j.rio.api;
    requires jakarta.ws.rs;
    requires com.fasterxml.jackson.databind;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires org.apache.httpcomponents.httpclient;
    requires nl.knaw.huc.sdswitch.recipe;
    requires rdf4j.model.vocabulary;
    provides nl.knaw.huc.sdswitch.recipe.Recipe with nl.knaw.huc.sdswitch.textanno.TextAnnoRecipe;
    opens nl.knaw.huc.sdswitch.textanno to com.fasterxml.jackson.databind;
}
