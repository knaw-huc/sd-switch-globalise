package org.knaw.huc.sdswitch.server.security.data;

import org.knaw.huc.auth.data.Tokens;
import org.knaw.huc.auth.data.UserInfo;

import java.time.Instant;
import java.util.UUID;

public class User {
    private final UserInfo userInfo;
    private final UUID apiKey;

    private Tokens tokens;
    private Instant refreshedAt;

    public User(UserInfo userInfo) {
        this(userInfo, null);
    }

    public User(UserInfo userInfo, Tokens tokens) {
        this.userInfo = userInfo;
        this.tokens = tokens;
        this.refreshedAt = Instant.now();
        this.apiKey = UUID.randomUUID();
    }

    public void setTokens(Tokens tokens) {
        this.tokens = tokens;
        this.refreshedAt = Instant.now();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public Tokens getTokens() {
        return tokens;
    }

    public UUID getApiKey() {
        return apiKey;
    }

    public boolean isAccessTokenExpired() {
        return tokens != null && this.refreshedAt.plusSeconds(tokens.expiresIn()).isBefore(Instant.now());
    }
}
