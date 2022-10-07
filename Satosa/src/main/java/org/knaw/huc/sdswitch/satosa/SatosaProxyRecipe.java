package org.knaw.huc.sdswitch.satosa;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.auth.OpenID;
import org.knaw.huc.auth.data.Essential;
import org.knaw.huc.auth.data.Tokens;
import org.knaw.huc.auth.data.UserInfo;
import org.knaw.huc.sdswitch.recipe.Recipe;
import org.knaw.huc.sdswitch.recipe.RecipeData;
import org.knaw.huc.sdswitch.recipe.RecipeParseException;
import org.knaw.huc.sdswitch.recipe.RecipeResponse;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nl.mpi.tla.util.Saxon;

public class SatosaProxyRecipe implements Recipe<SatosaProxyRecipe.SatosaProxyConfig> {
    record SatosaProxyConfig(OpenID openID) {
    }

    @Override
    public SatosaProxyConfig parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        try {
            final URI oidcServer = URI.create(System.getenv().get("OIDC_SERVER"));
            final URI applicationUrl = URI.create(System.getenv().getOrDefault("APPLICATION_URL", "http://localhost"));
            final URI redirectUrl = UriBuilder.fromUri(applicationUrl).path("/redirect").build();

            final String clientId = Saxon.xpath2string(config, "satosa/@clientid");
            final String clientSecret = Saxon.xpath2string(config, "satosa/@clientsecret");

            Map<String, Essential> userInfoClaims = new HashMap<>();
            userInfoClaims.put("edupersontargetedid", null);
            userInfoClaims.put("schac_home_organisation", null);
            userInfoClaims.put("nickname", null);
            userInfoClaims.put("email", null);
            userInfoClaims.put("eppn", null);
            userInfoClaims.put("idp", null);

            OpenID openID = new OpenID(oidcServer, redirectUrl, clientId, clientSecret,
                    Map.of("userinfo", userInfoClaims), "openid", "email", "profile");

            return new SatosaProxyConfig(openID);
        } catch(Exception e) {
            System.err.println("ERR: "+e.getMessage());
            return null;
        }
    }

    @Override
    public RecipeResponse withData(RecipeData<SatosaProxyConfig> data) {
        try {
            UserInfo userInfo = null;
            if (data.queryParams().containsKey("code")) {
                // 1. check if we can access user info, if so use it, else
                Tokens tokens = data.config().openID().getTokens(data.queryParams().get("code").get(0), false);
                userInfo = data.config().openID().getUserInfo(tokens.accessToken());
            } else if (data.headers().containsKey("Authorization")) {
                String auth = data.headers().get("Authorization").replaceFirst("^Basic:", "").trim();

                // 2. check if there is a delegation token, if so use it, else
                // 3. check if there is an API key, if so use it, else
                //userInfo = ...
            } else {
                // 4. login via OAuth, we'll come back via 1.

                // Start login flow
                String state = UUID.randomUUID().toString();
                return RecipeResponse.withRedirect(data.config().openID().createAuthUri(state).toString());
            }
            //check userInfo against white/blacklist
        } catch(Exception e) {
            System.err.println("ERR: "+e.getMessage());
        }

        //if allowed proxy ..
        return null;



    }
}
