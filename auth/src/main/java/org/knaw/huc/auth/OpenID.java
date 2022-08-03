package org.knaw.huc.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.knaw.huc.auth.data.Essential;
import org.knaw.huc.auth.data.Tokens;
import org.knaw.huc.auth.data.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenID {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenID.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);

    private final URI oidcServer;
    private final URI redirectUri;
    private final String clientId;
    private final String clientSecret;
    private final String claims;
    private final String[] scope;

    public OpenID(URI oidcServer, URI redirectUri, String clientId, String clientSecret,
                  Map<String, Map<String, Essential>> claims, String... scope) {
        try {
            this.oidcServer = oidcServer;
            this.redirectUri = redirectUri;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.claims = MAPPER.writeValueAsString(claims);
            this.scope = scope;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public URI createUserInfoUri() {
        return UriBuilder.fromUri(oidcServer)
                .path("OIDC/userinfo")
                .build();
    }

    public URI createAuthUri(Object state) {
        return UriBuilder.fromUri(oidcServer)
                .path("Saml2/OIDC/authorization")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", String.join(" ", scope))
                .queryParam("state", state)
                .queryParam("claims", URLEncoder.encode(claims, StandardCharsets.UTF_8))
                .build();
    }

    public URI createTokensUri() {
        return UriBuilder.fromUri(oidcServer)
                .path("OIDC/token")
                .build();
    }

    public String createTokensQuery(String code) {
        return createQueryString(Map.of(
                "client_id", clientId,
                "grant_type", "authorization_code",
                "redirect_uri", redirectUri.toString(),
                "code", code
        ));
    }

    public String createRefreshTokensQuery(String refreshToken) {
        return createQueryString(Map.of(
                "client_id", clientId,
                "grant_type", "refresh_token",
                "redirect_uri", redirectUri.toString(),
                "refresh_token", refreshToken
        ));
    }

    public UserInfo getUserInfo(String accessToken) throws OpenIDException, OpenIDUnauthorizedException {
        try {
            URI userInfoUri = createUserInfoUri();
            HttpURLConnection conn = (HttpURLConnection) userInfoUri.toURL().openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("authorization", "Bearer " + accessToken);

            LOGGER.info(String.format("Execute OpenID user info request %s with access token %s%n", userInfoUri, accessToken));

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                    throw new OpenIDUnauthorizedException();
                else
                    throw new OpenIDException("Failed to execute OpenID user info request with response code: " + conn.getResponseCode());
            }

            return MAPPER.readValue(conn.getInputStream(), UserInfo.class);
        } catch (IOException ex) {
            throw new OpenIDException("Failed to execute OpenID user info request", ex);
        }
    }

    public Tokens getTokens(String code, boolean isRefresh) throws OpenIDException, OpenIDUnauthorizedException {
        try {
            URI tokenUri = createTokensUri();
            HttpURLConnection conn = (HttpURLConnection) tokenUri.toURL().openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            String authValue = URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    ':' + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
            String encodedAuthValue = Base64.getEncoder().encodeToString(authValue.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("authorization", "Basic " + encodedAuthValue);

            String tokensQuery = isRefresh ? createRefreshTokensQuery(code) : createTokensQuery(code);
            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.write(tokensQuery.getBytes(StandardCharsets.UTF_8));
            }

            LOGGER.info(String.format("Execute OpenID tokens request %s and query %s", tokenUri, tokensQuery));

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
                    throw new OpenIDUnauthorizedException();
                else
                    throw new OpenIDException(String.format(
                            "Failed to execute OpenID tokens request with response code: %s and message %s",
                            conn.getResponseCode(), new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8)));
            }

            return MAPPER.readValue(conn.getInputStream(), Tokens.class);
        } catch (IOException ex) {
            throw new OpenIDException("Failed to execute OpenID tokens request", ex);
        }
    }

    private static String createQueryString(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}
