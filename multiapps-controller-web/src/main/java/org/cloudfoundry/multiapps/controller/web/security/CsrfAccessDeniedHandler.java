package org.cloudfoundry.multiapps.controller.web.security;

import java.io.IOException;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cloudfoundry.multiapps.controller.web.Constants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

@Named("accessDeniedHandler")
public class CsrfAccessDeniedHandler extends OAuth2AccessDeniedHandler {

    private static final String CSRF_TOKEN_REQUIRED_HEADER_VALUE = "Required";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException authException)
        throws IOException, ServletException {
        if (authException instanceof InvalidCsrfTokenException || authException instanceof MissingCsrfTokenException) {
            response.setHeader(Constants.CSRF_TOKEN, CSRF_TOKEN_REQUIRED_HEADER_VALUE);
        }
        doHandle(request, response, authException);
    }

}
