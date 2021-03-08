package org.cloudfoundry.multiapps.controller.web.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.cloudfoundry.multiapps.controller.core.util.ApplicationConfiguration;

import com.sap.cloudfoundry.client.facade.CloudCredentials;
import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;
import com.sap.cloudfoundry.client.facade.oauth2.OAuthClient;
import com.sap.cloudfoundry.client.facade.util.RestUtil;

public class BasicTokenParsingStrategy implements TokenParsingStrategy {

    private final RestUtil restUtil = new RestUtil();
    private final ApplicationConfiguration applicationConfiguration;

    public BasicTokenParsingStrategy(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public OAuth2AccessTokenWithAdditionalInfo parseToken(String tokenString) {
        String credentials = new String(Base64.getDecoder()
                                              .decode(tokenString),
                                        StandardCharsets.UTF_8);
        String[] usernameAndPassword = credentials.split(":");
        String username = usernameAndPassword[0];
        String password = usernameAndPassword[1];
        OAuthClient oauthClient = restUtil.createOAuthClientByControllerUrl(applicationConfiguration.getControllerUrl(),
                                                                            applicationConfiguration.shouldSkipSslValidation());
        oauthClient.init(new CloudCredentials(username, password));
        return oauthClient.getToken();
    }
}
