package org.knaw.huc.auth.data;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public record UserInfo(String sub,
                       @JsonAnySetter Map<String, String> claims) {
    @JsonCreator
    UserInfo(@JsonProperty("sub") String sub) {
        this(sub, new HashMap<>());
    }
}
