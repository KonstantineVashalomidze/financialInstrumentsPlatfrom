package org.devexperts.interceptor;

import org.apache.el.parser.Token;
import org.devexperts.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/* Example WebSocket URL:
 * ws://localhost:8081/ws/subscribe?token=JWT_TOKEN
 */
@Component
public class JwtWebSocketInterceptor implements HandshakeInterceptor {

    private TokenCache tokenCache;
    private TokenExtractor tokenExtractor;
    public JwtWebSocketInterceptor(TokenCache tokenCache) {
        tokenExtractor = new WebSocketTokenExtractor();
        this.tokenCache = tokenCache;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = tokenExtractor.extractToken(request);
        if (token != null) {
            String username = tokenCache.validateToken(token);
            if (username != null) {
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

}
