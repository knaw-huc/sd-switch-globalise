package org.knaw.huc.sdswitch.server.security;

import io.javalin.Javalin;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.knaw.huc.auth.OpenID;
import org.knaw.huc.auth.OpenIDException;
import org.knaw.huc.auth.OpenIDUnauthorizedException;
import org.knaw.huc.auth.data.Tokens;
import org.knaw.huc.auth.data.UserInfo;
import org.knaw.huc.sdswitch.server.security.data.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityRouter implements AccessManager {
    private enum Code {AUTH_CODE, ACCESS_TOKEN, REFRESH_TOKEN}

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRouter.class);

    private final OpenID openID;
    private final Map<String, User> accessTokenUsers;
    private final Map<String, User> apiKeysUsers;
    private final Map<UUID, String> stateRedirects;

    public SecurityRouter(OpenID openID) {
        this.openID = openID;
        this.accessTokenUsers = new ConcurrentHashMap<>();
        this.apiKeysUsers = new ConcurrentHashMap<>();
        this.stateRedirects = new ConcurrentHashMap<>();
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", this::withLoginRequest, Role.ANONYMOUS);
        app.get("/redirect", this::withRedirectRequest, Role.ANONYMOUS);
        app.get("/apikey", this::withApiKeyRequest, Role.USER);
    }

    @Override
    public void manage(Handler handler, Context ctx, Set<RouteRole> routeRoles) throws Exception {
        String auth = ctx.header("Authorization");
        if (auth != null)
            auth = auth.replaceFirst("^Basic:", "").trim();
        else {
            auth = ctx.queryParam("access_token");
            if (auth != null)
                auth = auth.trim();
            else {
                auth = ctx.cookie("access_token");
                if (auth != null)
                    auth = auth.trim();
            }
        }

        LOGGER.info(String.format("Incoming request with access token %s", auth));

        User user = null;
        if (auth != null && !auth.isBlank()) {
            user = accessTokenUsers.get(auth);
            if (user == null) {
                user = apiKeysUsers.get(auth);
                if (user == null) {
                    user = getUserByCode(auth, Code.ACCESS_TOKEN);
                }
            }
        }

        withUser(ctx, user);

        RouteRole role = user != null ? Role.USER : Role.ANONYMOUS;
        if (!routeRoles.contains(role))
            throw new UnauthorizedResponse();

        handler.handle(ctx);
    }

    private void withLoginRequest(Context ctx) {
        if (ctx.queryParam("redirect-uri") == null)
            throw new BadRequestResponse("Missing 'redirect-uri'");

        UUID state = UUID.randomUUID();
        stateRedirects.put(state, ctx.queryParam("redirect-uri"));

        LOGGER.info(String.format("Perform OpenID auth request %s", openID.createAuthUri(state)));

        ctx.redirect(openID.createAuthUri(state).toString());
    }

    private void withRedirectRequest(Context ctx) {
        String stateParam = ctx.queryParam("state");
        if (stateParam == null)
            throw new BadRequestResponse("Missing 'state'");

        UUID state = UUID.fromString(stateParam);
        if (!stateRedirects.containsKey(state))
            throw new BadRequestResponse("Unknown 'state'");

        String code = ctx.queryParam("code");
        if (code == null)
            throw new BadRequestResponse("Missing 'code'");

        User user = getUserByCode(code, Code.AUTH_CODE);
        withUser(ctx, user);

        URI redirectUri = UriBuilder.fromUri(stateRedirects.remove(state))
                .queryParam("access_token", user.getTokens().accessToken())
                .build();

        ctx.redirect(redirectUri.toString());
    }

    private void withApiKeyRequest(Context ctx) {
        ctx.result(((User) ctx.attribute("user")).getApiKey().toString());
    }

    private User getUserByCode(String code, Code codeType) {
        return getUserByCode(code, codeType, null);
    }

    private User getUserByCode(String code, Code codeType, User user) {
        try {
            if (user != null) {
                accessTokenUsers.remove(user.getTokens().accessToken());
                apiKeysUsers.remove(user.getApiKey().toString());
            }

            Tokens tokens = null;
            String accessToken;

            if (codeType == Code.AUTH_CODE || codeType == Code.REFRESH_TOKEN) {
                tokens = openID.getTokens(code, codeType == Code.REFRESH_TOKEN);
                accessToken = tokens.accessToken();
                LOGGER.info(String.format("Obtained OpenID tokens %s", tokens));
            }
            else {
                accessToken = code;
            }

            if (user == null) {
                UserInfo userInfo = openID.getUserInfo(accessToken);
                user = new User(userInfo, tokens);
                LOGGER.info(String.format("Obtained user info %s", userInfo));
            }
            else
                user.setTokens(tokens);

            accessTokenUsers.put(accessToken, user);
            if (codeType != Code.ACCESS_TOKEN) {
                apiKeysUsers.put(user.getApiKey().toString(), user);
            }

            return user;
        } catch (OpenIDException oide) {
            LOGGER.error("Failed OpenID request", oide);
            return null;
        } catch (OpenIDUnauthorizedException oidue) {
            return null;
        }
    }

    private void withUser(Context ctx, User user) {
        if (user != null) {
            if (user.isAccessTokenExpired())
                user = getUserByCode(user.getTokens().refreshToken(), Code.REFRESH_TOKEN, user);

            ctx.attribute("user", user);

            if (user.getTokens() != null)
                ctx.cookie("access_token", user.getTokens().accessToken());
        }
        else
            ctx.removeCookie("access_token");
    }
}
