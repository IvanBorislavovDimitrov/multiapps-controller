package org.cloudfoundry.multiapps.controller.core.security.token;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.multiapps.common.SLException;
import org.cloudfoundry.multiapps.controller.core.security.token.parsers.TokenParserChain;
import org.cloudfoundry.multiapps.controller.persistence.model.AccessToken;
import org.cloudfoundry.multiapps.controller.persistence.services.AccessTokenService;

import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;

/**
 * Provides functionality for persisting, updating and removing tokens from a token store
 */
@Named
public class TokenService {

    private final AccessTokenService accessTokenService;
    private final TokenParserChain tokenParserChain;

    @Inject
    public TokenService(AccessTokenService accessTokenService, TokenParserChain tokenParserChain) {
        this.accessTokenService = accessTokenService;
        this.tokenParserChain = tokenParserChain;
    }

    /**
     * Chooses a token among all tokens for this user in the token store.
     *
     * @param userName the username
     * @return the chosen token, or null if no token was found
     */
    public OAuth2AccessTokenWithAdditionalInfo getToken(String userName) {
        // OAuth2AccessToken token = null;
        // Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByUserName(userName);
        // for (OAuth2AccessToken tokenx : tokens) {
        // // If a token is already found, overwrite it if the new token:
        // // 1) has a refresh token, and the current token hasn't, or
        // // 2) expires later than the current token
        // if (token == null || ((tokenx.getRefreshToken() != null) && (token.getRefreshToken() == null))
        // || (tokenx.getExpiresIn() > token.getExpiresIn())) {
        // token = tokenx;
        // }
        // }

        System.out.println("INICIALIZIRAH: " + accessTokenService);

        System.out.println("E TOQ BOKLUK " + userName);

        System.out.println("OT BAZATA GOOGO");

        List<AccessToken> accessTokens = accessTokenService.createQuery()
                                                           .username(userName)
                                                           .list()
                                                           .stream()
                                                           .sorted((s1, s2) -> s2.getExpiresAt()
                                                                                 .compareTo(s1.getExpiresAt()))
                                                           .collect(Collectors.toList());
        AccessToken accessToken = accessTokens.get(0);

        OAuth2AccessTokenWithAdditionalInfo tokenByUser = tokenParserChain.parse(new String(accessToken.getValue(),
                                                                                            StandardCharsets.UTF_8));
        System.out.println("TRIEME");

        accessTokens.stream()
                    .skip(1)
                    .forEach(token -> {
                        System.out.println("KURVI EBEME SE: " + token.getId());
                        accessTokenService.createQuery()
                                          .id(token.getId())
                                          .delete();
                    });

        if (tokenByUser == null) {
            throw new SLException("Token for user not found");
        }
        return tokenByUser;
    }

    /**
     * Removes specific token from the tokenStore
     *
     * @param token the token to be removed from the database
     */
    public void removeToken(OAuth2AccessTokenWithAdditionalInfo token) {
        // tokenStore.removeAccessToken(token);
    }
}
