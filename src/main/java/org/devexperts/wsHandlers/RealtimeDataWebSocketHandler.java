package org.devexperts.wsHandlers;

import org.devexperts.service.InstrumentDataService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class RealtimeDataWebSocketHandler extends TextWebSocketHandler {

    private final InstrumentDataService instrumentDataService;


    public RealtimeDataWebSocketHandler(InstrumentDataService instrumentDataService) {
        this.instrumentDataService = instrumentDataService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = (String) session.getAttributes()
                .get("username");
        instrumentDataService.startSendingUpdates(
                username,
                session
        );
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        String username = (String) session.getAttributes()
                .get("username");
        instrumentDataService.stopSendingUpdates(
                username
        );
    }
}