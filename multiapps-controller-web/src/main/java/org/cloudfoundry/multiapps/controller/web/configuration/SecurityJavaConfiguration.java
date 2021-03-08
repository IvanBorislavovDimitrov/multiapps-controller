package org.cloudfoundry.multiapps.controller.web.configuration;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.cloudfoundry.multiapps.controller.web.security.AuthorizationLoaderFilter;
import org.cloudfoundry.multiapps.controller.web.security.CompositeUriAuthorizationFilter;
import org.cloudfoundry.multiapps.controller.web.security.CsrfHeadersFilter;
import org.cloudfoundry.multiapps.controller.web.security.RequestSizeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.csrf.CsrfFilter;

@EnableWebSecurity
@Configuration
public class SecurityJavaConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private AuthorizationLoaderFilter authorizationLoaderFilter;
    @Inject
    private CompositeUriAuthorizationFilter compositeUriAuthorizationFilter;
    @Inject
    private RequestSizeFilter requestSizeFilter;
    @Inject
    private CsrfHeadersFilter csrfHeadersFilter;
    @Inject
    private DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        System.out.println("TUKE E 1 POEDAL");

        System.out.println("adsadas " + authorizationLoaderFilter);
        System.out.println("putko " + compositeUriAuthorizationFilter);
        System.out.println("vanko1 " + requestSizeFilter);
        System.out.println("vanko2 " + csrfHeadersFilter);
        System.out.println("vanko3 " + dataSource);

        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/api/**")
            .hasAnyAuthority("cloud_controller.read", "cloud_controller.admin")
            .antMatchers(HttpMethod.POST, "/api/**")
            .hasAnyAuthority("cloud_controller.write", "cloud_controller.admin")
            .antMatchers(HttpMethod.PUT, "/api/**")
            .hasAnyAuthority("cloud_controller.write", "cloud_controller.admin")
            .antMatchers(HttpMethod.DELETE, "/api/**")
            .hasAnyAuthority("cloud_controller.write", "cloud_controller.admin")
            .and()
            .addFilterBefore(authorizationLoaderFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .addFilterAfter(requestSizeFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .addFilterAfter(csrfHeadersFilter, CsrfFilter.class)
            .addFilterAfter(compositeUriAuthorizationFilter, SwitchUserFilter.class);

        http.httpBasic()
            .and()
            .authorizeRequests()
            .antMatchers("/slprot/**")
            .authenticated();

        // http
        // .sessionManagement()
        // .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        // .and()
        // .authorizeRequests()
        // .antMatchers("/public/**").permitAll()
        // .antMatchers("/helloReader")
        // .hasAuthority("Reader")
        // .antMatchers("/helloWriter")
        // .hasAuthority("Writer")
        // .antMatchers("/helloAdministrator")
        // .hasAuthority("Administrator")
        // .antMatchers("/callback/v1.0/**")
        // .hasAuthority("Callback")
        // .antMatchers(HttpMethod.GET, "/**")
        // .hasAnyAuthority("SCOPE_cloud_controller.read", "SCOPE_cloud_controller.admin")
        // .antMatchers(HttpMethod.PUT, "/**")
        // .hasAnyAuthority("SCOPE_cloud_controller.write", "SCOPE_cloud_controller.admin")
        // .antMatchers(HttpMethod.POST, "/**")
        // .hasAnyAuthority("SCOPE_cloud_controller.write", "SCOPE_cloud_controller.admin")
        // .antMatchers(HttpMethod.DELETE, "/**")
        // .hasAnyAuthority("SCOPE_cloud_controller.write", "SCOPE_cloud_controller.admin")
        // // .denyAll()
        // .and()
        // .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri("https://uaa.cf.sap.hana.ondemand.com")))
        // .addFilterBefore(oauth2TokenParserFilter, AbstractPreAuthenticatedProcessingFilter.class);
        // .addFilterAfter(requestSizeFilter, AbstractPreAuthenticatedProcessingFilter.class)
        // .addFilterAfter(csrfHeadersFilter, CsrfFilter.class)
        // .addFilterAfter(csrfHeadersFilter, AbstractPreAuthenticatedProcessingFilter.class)
        // .addFilterAfter(compositeUriAuthorizationFilter, SwitchUserFilter.class);

        // http
        // .authorizeRequests(authorize -> authorize
        // .anyRequest().authenticated()
        // )
        // .oauth2ResourceServer(oauth2 -> oauth2
        // .jwt(jwt -> jwt
        // .jwkSetUri("https://idp.example.com/.well-known/jwks.json")
        // )
        // );
    }

    // @Override
    // public void configure(WebSecurity web) {
    // web.ignoring()
    // .antMatchers("/public/**");
    // }

    // @Override
    // public void configure(AuthenticationManagerBuilder auth) throws Exception {
    // auth.jdbcAuthentication()
    // .dataSource(dataSource);
    // }

    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // auth.authenticationProvider(customAuthenticationProvider);
    // }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CsrfAccessDeniedHandler();
    }

}
