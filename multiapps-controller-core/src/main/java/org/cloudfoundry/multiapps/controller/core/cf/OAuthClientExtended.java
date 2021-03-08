package org.cloudfoundry.multiapps.controller.core.cf;

import java.net.URL;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.cloudfoundry.multiapps.controller.client.util.TokenProperties;
import org.cloudfoundry.multiapps.controller.core.Messages;
import org.cloudfoundry.multiapps.controller.core.security.token.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;
import com.sap.cloudfoundry.client.facade.oauth2.OAuthClient;

public class OAuthClientExtended extends OAuthClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthClientExtended.class);
    private final TokenService tokenService;

    public OAuthClientExtended(URL authorizationUrl, TokenService tokenService, WebClient webClient) {
        super(authorizationUrl, webClient);
        this.tokenService = tokenService;
    }

    @Override
    public OAuth2AccessTokenWithAdditionalInfo getToken() {
        if (token == null) {
            return null;
        }

        System.out.println("TOKEN EXPIRES: " + token.getOAuth2AccessToken().getExpiresAt());
        System.out.println("SEGA 30 E: " + Instant.now()
                .minus(30, ChronoUnit.SECONDS));
        System.out.println("SEGA TOCHNO e " + Instant.now() );

        System.out.println("ZASHTO BE IVANE: " + token.getOAuth2AccessToken()
                .getExpiresAt()
                .isBefore(Instant.now()
                        .plus(30, ChronoUnit.SECONDS)));

        if (token.getOAuth2AccessToken()
                 .getExpiresAt()
                 .isBefore(Instant.now()
                                  .plus(30, ChronoUnit.SECONDS))) {

            System.out.println("IZTECHE LI DA GO EBA");

            TokenProperties tokenProperties = TokenProperties.fromToken(token);
            token = tokenService.getToken(tokenProperties.getUserName());
            LOGGER.info(MessageFormat.format(Messages.RETRIEVED_TOKEN_FOR_USER_0_WITH_EXPIRATION_TIME_1, tokenProperties.getUserName(),
                                             token.getOAuth2AccessToken()
                                                  .getExpiresAt()));
        }

        System.out.println("DAVAM GEISKI TOKEN " + token.getOAuth2AccessToken().getTokenValue());

        return token;
    }
}
