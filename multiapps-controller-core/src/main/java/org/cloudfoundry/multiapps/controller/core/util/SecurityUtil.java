package org.cloudfoundry.multiapps.controller.core.util;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.cloudfoundry.multiapps.controller.client.util.TokenProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;

public class SecurityUtil {

    public static final String CLIENT_ID = "cf";
    public static final String CLIENT_SECRET = "";

    private SecurityUtil() {
    }

    public static UsernamePasswordAuthenticationToken createAuthentication(Set<String> scope, UserInfo userInfo) {
        List<SimpleGrantedAuthority> authorities = getAuthorities(scope);
        return new UsernamePasswordAuthenticationToken(userInfo, "", authorities);
    }

    private static List<SimpleGrantedAuthority> getAuthorities(Set<String> scopes) {
        return scopes.stream()
                     .map(SimpleGrantedAuthority::new)
                     .collect(Collectors.toList());
    }

    public static UserInfo getTokenUserInfo(OAuth2AccessTokenWithAdditionalInfo token) {
        TokenProperties tokenProperties = TokenProperties.fromToken(token);
        return new UserInfo(tokenProperties.getUserId(), tokenProperties.getUserName(), token);
    }

}
