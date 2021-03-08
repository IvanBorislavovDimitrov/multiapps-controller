package org.cloudfoundry.multiapps.controller.web.util;

import java.text.MessageFormat;

import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.core.security.token.parsers.TokenParserChain;
import org.cloudfoundry.multiapps.controller.core.util.ApplicationConfiguration;

@Named
public class TokenParsingStrategyFactory {

    private final ApplicationConfiguration applicationConfiguration;
    private final TokenParserChain tokenParserChain;

    public TokenParsingStrategyFactory(ApplicationConfiguration applicationConfiguration, TokenParserChain tokenParserChain) {
        this.applicationConfiguration = applicationConfiguration;
        this.tokenParserChain = tokenParserChain;
    }

    private static final String BASIC_TOKEN_TYPE = "basic";
    private static final String BEARER_TOKEN_TYPE = "bearer";

    public TokenParsingStrategy createTokenParsingStrategy(String tokenType) {
        if (BASIC_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
            return new BasicTokenParsingStrategy(applicationConfiguration);
        } else if (BEARER_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
            return new OauthTokenParsingStrategy(tokenParserChain);
        }
        throw new IllegalStateException(MessageFormat.format("Unsupported token type: \"{0}\".", tokenType));
    }
}
