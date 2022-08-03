module org.knaw.huc.auth {
    requires java.ws.rs;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    exports org.knaw.huc.auth;
    exports org.knaw.huc.auth.data;
    opens org.knaw.huc.auth.data to com.fasterxml.jackson.databind;
}
