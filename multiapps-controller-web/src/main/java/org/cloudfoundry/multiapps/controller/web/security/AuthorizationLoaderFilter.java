package org.cloudfoundry.multiapps.controller.web.security;

import static com.sap.cloudfoundry.client.facade.oauth2.TokenFactory.EXP;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.controller.core.security.token.parsers.TokenParserChain;
import org.cloudfoundry.multiapps.controller.core.util.ApplicationConfiguration;
import org.cloudfoundry.multiapps.controller.core.util.SecurityUtil;
import org.cloudfoundry.multiapps.controller.core.util.UserInfo;
import org.cloudfoundry.multiapps.controller.persistence.model.AccessToken;
import org.cloudfoundry.multiapps.controller.persistence.model.ImmutableAccessToken;
import org.cloudfoundry.multiapps.controller.persistence.services.AccessTokenService;
import org.cloudfoundry.multiapps.controller.web.util.TokenParsingStrategy;
import org.cloudfoundry.multiapps.controller.web.util.TokenParsingStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sap.cloudfoundry.client.facade.CloudCredentials;
import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;
import com.sap.cloudfoundry.client.facade.oauth2.OAuthClient;
import com.sap.cloudfoundry.client.facade.util.RestUtil;

@Named
public class AuthorizationLoaderFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationLoaderFilter.class);

    private final RestUtil restUtil = new RestUtil();

    private final AccessTokenService accessTokenService;
    private final ApplicationConfiguration configuration;
    private final TokenParsingStrategyFactory tokenParsingStrategyFactory;

    @Inject
    public AuthorizationLoaderFilter(AccessTokenService accessTokenService,
                                     ApplicationConfiguration configuration, TokenParsingStrategyFactory tokenParsingStrategyFactory) {
        this.accessTokenService = accessTokenService;
        this.configuration = configuration;
        this.tokenParsingStrategyFactory = tokenParsingStrategyFactory;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        System.out.println("PISHKIIIIIIII");
        String tokenStringWithType = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenStringWithType == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String[] typeWithAuthorization = tokenStringWithType.split("\\s");
        TokenParsingStrategy tokenParsingStrategy = tokenParsingStrategyFactory.createTokenParsingStrategy(typeWithAuthorization[0]);
        OAuth2AccessTokenWithAdditionalInfo oAuth2AccessTokenWithAdditionalInfo = tokenParsingStrategy.parseToken(typeWithAuthorization[1]);
        UserInfo tokenUserInfo = SecurityUtil.getTokenUserInfo(oAuth2AccessTokenWithAdditionalInfo);
        // TODO: Try to use OAuthAuthentication
        UsernamePasswordAuthenticationToken authentication = SecurityUtil.createAuthentication(oAuth2AccessTokenWithAdditionalInfo.getOAuth2AccessToken()
                                                                                                                                  .getScopes(),
                                                                                               tokenUserInfo);
        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);

        Date date = new Date(((Number) oAuth2AccessTokenWithAdditionalInfo.getAdditionalInfo()
                                                                          .get(EXP)).longValue()
            * 1000);

        Date dateMinus2Min = new Date(((Number) oAuth2AccessTokenWithAdditionalInfo.getAdditionalInfo()
                                                                                   .get(EXP)).longValue()
            * 1000 - (120 * 1000));

        System.out.println("TOGAWA IZTICHA: " + date);
        System.out.println("TOGAWA IZTICHA DGE: " + dateMinus2Min);

        AccessToken accessToken = ImmutableAccessToken.builder()
                                                      .value(oAuth2AccessTokenWithAdditionalInfo.getOAuth2AccessToken()
                                                                                                .getTokenValue()
                                                                                                .getBytes(StandardCharsets.UTF_8))
                                                      .username(tokenUserInfo.getName())
                                                      .expiresAt(date)
                                                      .build();

        List<AccessToken> list = accessTokenService.createQuery()
                                                   .username(tokenUserInfo.getName())
                                                   .greaterThan(dateMinus2Min)
                                                   .list();

        if (list.size() == 0) {
            System.out.println("SQ GO DOBAVAM");
            AccessToken add = accessTokenService.add(accessToken);
            System.out.println("IMA POVTORNIE");
        }
        System.out.println("STAVA PEDALA " + list);

        System.out.println("TOQ PEDERAS TREA SE DOBAVI");

        System.out.println(authentication.getPrincipal() + "  KORO");

        filterChain.doFilter(request, response);
    }

    //
    // private final TokenStore tokenStore;
    // private final TokenParserChain tokenParserChain;
    //
    // @Inject
    // public CustomTokenServices(TokenStore tokenStore, ApplicationConfiguration configuration, TokenParserChain tokenParserChain) {
    // this.tokenStore = tokenStore;
    // this.tokenParserChain = tokenParserChain;
    // if (configuration.shouldSkipSslValidation()) {
    // SSLUtil.disableSSLValidation();
    // }
    // }
    //
    // @Override
    // public OAuth2Authentication loadAuthentication(String tokenString) {
    //
    // // Get an access token for the specified token string
    // OAuth2AccessToken token = readAccessToken(tokenString);
    //
    // // Check if a valid access token has been obtained
    // if (token == null) {
    // logToAuditLogAndThrow("Invalid access token");
    // }
    //
    // // Check if the token has expired and there is no refresh token
    // if (token.isExpired() && token.getRefreshToken() == null) {
    // tokenStore.removeAccessToken(token);
    // logToAuditLogAndThrow(MessageFormat.format("The access token has expired on {0}", token.getExpiration()));
    // }
    //
    // // Check if an authentication for this token already exists in the token store
    // OAuth2Authentication auth = tokenStore.readAuthentication(token);
    // if (auth == null) {
    // // Create an authentication for the token and store it in the token store
    // TokenProperties tokenProperties = TokenProperties.fromToken(token);
    // auth = SecurityUtil.createAuthentication(tokenProperties.getClientId(), token.getScope(), SecurityUtil.getTokenUserInfo(token));
    // try {
    // LOGGER.info(MessageFormat.format(Messages.STORING_TOKEN_FOR_USER_0_WITH_EXPIRATION_TIME_1, tokenProperties.getUserName(),
    // token.getExpiresIn()));
    // tokenStore.storeAccessToken(token, auth);
    // } catch (DataIntegrityViolationException e) {
    // LOGGER.debug(Messages.ERROR_STORING_TOKEN_DUE_TO_INTEGRITY_VIOLATION, e);
    // // Ignoring the exception as the token and authentication are already persisted by another client.
    // }
    // }
    //
    // return auth;
    // }
    //
    // @Override
    // public OAuth2AccessToken readAccessToken(String tokenString) {
    // // Check if an access token for the received token string already exists in the token store
    // OAuth2AccessToken token = tokenStore.readAccessToken(tokenString);
    // if (token != null) {
    // LOGGER.debug("Stored token value: " + token.getValue());
    // LOGGER.debug("Stored token type: " + token.getTokenType());
    // LOGGER.debug("Stored token expires in: " + token.getExpiresIn());
    // } else {
    // token = tokenParserChain.parse(tokenString);
    // }
    // return token;
    // }
    //
    // private void logToAuditLogAndThrow(String message) {
    // AuditLoggingProvider.getFacade()
    // .logSecurityIncident(message);
    // throw new InvalidTokenException(message);
    // }

}
