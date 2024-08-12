package org.devexperts.interceptor;

import org.devexperts.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/* Example WebSocket URL:
 * ws://localhost:8081/ws/subscribe?token=JWT_TOKEN
 */
public class JwtWebSocketInterceptor implements HandshakeInterceptor {

    private JwtUtil jwtUtil;

    public JwtWebSocketInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = extractToken(request);
        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            if (jwtUtil.validateToken(token, username)) {
                attributes.put("username", username);
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {

    }

    private String extractToken(ServerHttpRequest request) {
        // Extract the query parameters from the request URI
        Map<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().toSingleValueMap();

        // Retrieve the token from the query parameters
        return queryParams.get("token");
    }
}
