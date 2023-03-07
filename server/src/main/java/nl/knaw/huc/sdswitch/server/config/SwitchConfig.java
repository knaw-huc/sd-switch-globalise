package nl.knaw.huc.sdswitch.server.config;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public interface SwitchConfig {
    String recipe();

    Set<Url> urls();

    JsonNode config();

    record ParentSwitchConfig(Set<SubSwitchConfig> subSwitches,
                              String recipe,
                              Set<Url> urls,
                              JsonNode config) implements SwitchConfig {
    }

    record SubSwitchConfig(String recipe,
                           Set<Url> urls,
                           JsonNode config) implements SwitchConfig {
    }

    record Url(String pattern,
               String accept) {
    }
}
