package org.knaw.huc.auth.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Tokens(@JsonProperty("access_token") String accessToken,
                     @JsonProperty("token_type") String tokenType,
                     @JsonProperty("refresh_token") String refreshToken,
                     @JsonProperty("expires_in") long expiresIn,
                     @JsonProperty("refresh_expires_in") long refreshExpireIn,
                     @JsonProperty("not-before-policy") String notBeforePolicy,
                     @JsonProperty("id_token") String idToken) {
}
