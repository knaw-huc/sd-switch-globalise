package org.knaw.huc.auth.data;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record Tokens(@JsonProperty("access_token") String accessToken,
                     @JsonProperty("token_type") String tokenType,
                     @JsonProperty("refresh_token") String refreshToken,
                     @JsonProperty("expires_in") long expiresIn,
                     @JsonProperty("id_token") String idToken,
                     @JsonAnySetter Map<String, String> extra) {
}
