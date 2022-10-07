package org.knaw.huc.sdswitch.satosa;

import net.sf.saxon.s9api.XdmItem;
import org.knaw.huc.auth.OpenID;
import org.knaw.huc.auth.data.Essential;
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

public class SatosaProxyRecipe implements Recipe<SatosaProxyRecipe.SatosaProxyConfig> {
    record SatosaProxyConfig(OpenID openID) {
    }

    @Override
    public SatosaProxyConfig parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException {
        final URI oidcServer = URI.create(System.getenv().get("OIDC_SERVER"));
        final URI applicationUrl = URI.create(System.getenv().getOrDefault("APPLICATION_URL", "http://localhost"));
        final URI redirectUrl = UriBuilder.fromUri(applicationUrl).path("/redirect").build();

        final String clientId = System.getenv().get("OIDC_CLIENT_ID");
        final String clientSecret = System.getenv().get("OIDC_CLIENT_SECRET");

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
    }

    @Override
    public RecipeResponse withData(RecipeData<SatosaProxyConfig> data) {
        // Get auth key
        String auth = null;
        if (data.headers().containsKey("Authorization"))
            auth = data.headers().get("Authorization").replaceFirst("^Basic:", "").trim();

        // Start login flow
        if (auth == null) {
            String state = UUID.randomUUID().toString();
            return RecipeResponse.withRedirect(data.config().openID().createAuthUri(state).toString());
        }

        
    }
}
