package org.devexperts.interceptor;

import jakarta.servlet.http.HttpServletRequest;

public class HttpTokenExtractor implements TokenExtractor {
    @Override
    public String extractToken(Object request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
