package org.devexperts.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class WebSocketTokenExtractor implements TokenExtractor {
    @Override
    public String extractToken(Object request) {
        ServerHttpRequest serverRequest = (ServerHttpRequest) request;
        Map<String, String> queryParams = UriComponentsBuilder.fromUri(serverRequest.getURI()).build().getQueryParams().toSingleValueMap();
        return queryParams.get("token");
    }
}