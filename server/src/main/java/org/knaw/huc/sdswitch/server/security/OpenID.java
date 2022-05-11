package org.knaw.huc.sdswitch.server.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class OpenID {
    private final URI oidcServer;
    private final URI redirectUri;
    private final String clientId;
    private final String clientSecret;
    private final String[] scope;

    public OpenID(URI oidcServer, URI redirectUri, String clientId, String clientSecret) {
        this(oidcServer, redirectUri, clientId, clientSecret, new String[]{"openid", "email", "profile"});
    }

    public OpenID(URI oidcServer, URI redirectUri, String clientId, String clientSecret, String[] scope) {
        this.oidcServer = oidcServer;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    public URI createUserInfoUri() {
        return UriBuilder.fromUri(oidcServer)
                .path("/userinfo")
                .build();
    }

    public URI createAuthUri(Object state) {
        return UriBuilder.fromUri(oidcServer)
                .path("Saml2/OIDC/authorization")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build();
    }

    public URI createTokensUri(String code) {
        return UriBuilder.fromUri(oidcServer)
                .path("/token")
                .queryParam("client_id", clientId)
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", String.join(" ", scope))
                .build();
    }

    public HashMap<String, String> getUserInfo(String accessToken) throws OpenIDException {
        try {
            URI userInfoUri = createUserInfoUri();
            HttpURLConnection conn = (HttpURLConnection) userInfoUri.toURL().openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("authorization", "Basic " + accessToken);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new OpenIDException("Failed to execute OpenID user info request with response code: " + conn.getResponseCode());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(conn.getInputStream(), new TypeReference<>() {
            });
        } catch (IOException ex) {
            throw new OpenIDException("Failed to execute OpenID user info request", ex);
        }
    }

    public Tokens getTokens(String code) throws OpenIDException {
        try {
            URI tokenUri = createTokensUri(code);
            HttpURLConnection conn = (HttpURLConnection) tokenUri.toURL().openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            String authValue = URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    ':' + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
            String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("authorization", "Basic " + encodedAuthValue);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new OpenIDException("Failed to execute OpenID tokens request with response code: " + conn.getResponseCode());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(conn.getInputStream(), Tokens.class);
        } catch (IOException ex) {
            throw new OpenIDException("Failed to execute OpenID tokens request", ex);
        }
    }

    public record Tokens(@JsonProperty("access_token") String accessToken,
                         @JsonProperty("token_type") String tokenType,
                         @JsonProperty("refresh_token") String refreshToken,
                         @JsonProperty("expires_in") long expiresIn,
                         @JsonProperty("id_token") String idToken) {
    }
}
