package org.knaw.huc.sdswitch.server.security;

import io.javalin.Javalin;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.knaw.huc.sdswitch.server.util.Cache;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SecurityRouter implements AccessManager {
    private final OpenID openID;
    private final Cache<String, Map<String, String>> userInfoCache;
    private final Map<UUID, String> stateRedirects;

    public SecurityRouter(OpenID openID) {
        this.openID = openID;
        this.userInfoCache = new Cache<>();
        this.stateRedirects = new HashMap<>();
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", this::withLoginRequest, Role.ANONYMOUS);
        app.get("/redirect", this::withRedirectRequest, Role.ANONYMOUS);
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

        Map<String, String> userInfo = null;
        if (auth != null && !auth.isBlank()) {
            userInfo = userInfoCache.get(auth);
            if (userInfo == null) {
                userInfo = openID.getUserInfo(auth);
                if (userInfo != null)
                    userInfoCache.put(auth, userInfo);
            }
        }

        if (userInfo != null) {
            ctx.attribute("user", userInfo);
            ctx.cookie("access_token", auth);
        }

        RouteRole role = userInfo != null ? Role.USER : Role.ANONYMOUS;
        if (!routeRoles.contains(role))
            throw new UnauthorizedResponse();

        handler.handle(ctx);
    }

    private void withLoginRequest(Context ctx) {
        if (ctx.queryParam("redirect-uri") == null)
            throw new BadRequestResponse("Missing 'redirect-uri'");

        UUID state = UUID.randomUUID();
        stateRedirects.put(state, ctx.queryParam("redirect-uri"));

        ctx.redirect(openID.createAuthUri(state).toString());
    }

    private void withRedirectRequest(Context ctx) throws OpenIDException {
        if (ctx.queryParam("state") == null)
            throw new BadRequestResponse("Missing 'state'");

        UUID state = UUID.fromString(ctx.queryParam("state"));
        if (!stateRedirects.containsKey(state))
            throw new BadRequestResponse("Unknown 'state'");

        if (ctx.queryParam("code") == null)
            throw new BadRequestResponse("Missing 'code'");

        OpenID.Tokens tokens = openID.getTokens(ctx.queryParam("code"));

        URI redirectUri = UriBuilder.fromUri(stateRedirects.remove(state))
                .queryParam("access_token", tokens.accessToken())
                .build();

        ctx.redirect(redirectUri.toString());
    }
}
