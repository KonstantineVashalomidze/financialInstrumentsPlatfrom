package org.devexperts.config;

import org.devexperts.interceptor.JwtWebSocketInterceptor;
import org.devexperts.service.InstrumentDataService;
import org.devexperts.service.MessageService;
import org.devexperts.service.UserService;
import org.devexperts.util.JwtUtil;
import org.devexperts.wsHandlers.ChatHandler;
import org.devexperts.wsHandlers.InstrumentHandler;
import org.devexperts.wsHandlers.RealtimeDataWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private InstrumentDataService instrumentDataService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(
                        new ChatHandler(messageService),
                        "/ws/chat"
                )
                .addInterceptors(new JwtWebSocketInterceptor(jwtUtil))
                .setAllowedOrigins("http://localhost:3000");


        registry.addHandler(
                        new InstrumentHandler(instrumentDataService, userService),
                        "/ws/subscribe"
                )
                .addInterceptors(new JwtWebSocketInterceptor(jwtUtil))
                .setAllowedOrigins("http://localhost:3000");

        registry.addHandler(
                        new RealtimeDataWebSocketHandler(instrumentDataService),
                        "/ws/realtime"
                )
                .addInterceptors(new JwtWebSocketInterceptor(jwtUtil))
                .setAllowedOrigins("http://localhost:3000");
    }
}