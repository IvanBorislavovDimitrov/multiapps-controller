package org.cloudfoundry.multiapps.controller.web.util;

import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;
import org.cloudfoundry.multiapps.controller.core.security.token.parsers.TokenParserChain;

public class OauthTokenParsingStrategy implements TokenParsingStrategy {

    private final TokenParserChain tokenParserChain;

    public OauthTokenParsingStrategy(TokenParserChain tokenParserChain) {
        this.tokenParserChain = tokenParserChain;
    }

    // TODO VALIDATE TOKEN EXPIRATION
    @Override
    public OAuth2AccessTokenWithAdditionalInfo parseToken(String tokenString) {
        return tokenParserChain.parse(tokenString);

    }
}
