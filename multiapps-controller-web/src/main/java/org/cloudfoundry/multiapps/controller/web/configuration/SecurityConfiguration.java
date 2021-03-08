package org.cloudfoundry.multiapps.controller.web.configuration;

import java.util.Arrays;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
public class SecurityConfiguration {

    private static final String CF_DEPLOY_SERVICE = "CF Deploy Service";

    // @Inject
    // @Bean
    // public DelegatingAuthenticationEntryPoint
    // delegatingAuthenticationEntryPoint(OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint,
    // BasicAuthenticationEntryPoint basicAuthenticationEntryPoint) {
    // LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
    // entryPoints.put(this::containsBearerAuthorizationHeader, oauthAuthenticationEntryPoint);
    // DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
    // delegatingAuthenticationEntryPoint.setDefaultEntryPoint(basicAuthenticationEntryPoint);
    // return delegatingAuthenticationEntryPoint;
    // }
    //
    // private boolean containsBearerAuthorizationHeader(HttpServletRequest request) {
    // String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    // return StringUtils.startsWithIgnoreCase(authorizationHeader, "bearer");
    // }

    // @Bean
    // public OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint() {
    // OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
    // oauthAuthenticationEntryPoint.setRealmName(CF_DEPLOY_SERVICE);
    // return oauthAuthenticationEntryPoint;
    // }

//    @Bean
//    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
//        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
//        basicAuthenticationEntryPoint.setRealmName(CF_DEPLOY_SERVICE);
//        return basicAuthenticationEntryPoint;
//    }
//
//    @Bean("customHttpFirewall")
//    public HttpFirewall customHttpFirewall() {
//        DefaultHttpFirewall defaultHttpFirewall = new DefaultHttpFirewall();
//        defaultHttpFirewall.setAllowUrlEncodedSlash(true);
//        return defaultHttpFirewall;
//    }

//    @Inject
//    @Bean
//    public UnanimousBased accessDecisionManager(WebExpressionVoter webExpressionVoter, AuthenticatedVoter authenticatedVoter) {
//        return new UnanimousBased(Arrays.asList(webExpressionVoter, authenticatedVoter));
//    }
//
//    @Bean
//    public AuthenticatedVoter authenticatedVoter() {
//        return new AuthenticatedVoter();
//    }

    // @Inject
    // @Bean
    // public JdbcTokenStore tokenStore(DataSource dataSource) {
    // return TokenStoreFactory.getTokenStore(dataSource);
    // }

    // @Bean
    // public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(DataSource dataSource) {
    // JdbcOAuth2AuthorizedClientService jdbcOAuth2AuthorizedClientService =
    // new JdbcOAuth2AuthorizedClientService(new JdbcTemplate(dataSource), clientRegistrationRepository());
    // OAuth2AuthorizedClient oAuth2AuthorizedClient = jdbcOAuth2AuthorizedClientService.loadAuthorizedClient();
    // OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    // oAuth2AuthorizedClientService
    // }
    //
    // public ClientRegistrationRepository clientRegistrationRepository() {
    // return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    // }
    //
    // private ClientRegistration googleClientRegistration() {
    // return ClientRegistration.withRegistrationId("google")
    // .clientId("google-client-id")
    // .registrationId("google")
    // .clientSecret("google-client-secret")
    // .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
    // .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    // .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
    // .scope("openid", "profile", "email", "address", "phone")
    // .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
    // .tokenUri("https://www.googleapis.com/oauth2/v4/token")
    // .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
    // .userNameAttributeName(IdTokenClaimNames.SUB)
    // .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
    // .clientName("Google")
    // .build();
    // }

}
