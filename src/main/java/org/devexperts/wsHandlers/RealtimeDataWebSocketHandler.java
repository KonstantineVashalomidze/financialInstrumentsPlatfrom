package org.devexperts.wsHandlers;

import org.devexperts.controller.AuthController;
import org.devexperts.service.InstrumentDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class RealtimeDataWebSocketHandler extends TextWebSocketHandler {

    private final InstrumentDataService instrumentDataService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String myLog = "[!!!MY_LOG!!!]";

    public RealtimeDataWebSocketHandler(InstrumentDataService instrumentDataService) {
        this.instrumentDataService = instrumentDataService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = (String) session.getAttributes()
                .get("username");
        logger.info("{} User {} connected realtime with id {}", myLog, username, session.getId());
        instrumentDataService.startSendingUpdates(
                username,
                session
        );
        logger.info("{} Started sending updates to user {} with id {}", myLog, username, session.getId());
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        String username = (String) session.getAttributes()
                .get("username");
        logger.info("{} Closed connection for user {} with id {}", myLog, username, session.getId());
        instrumentDataService.stopSendingUpdates(
                username,
                session
        );
        logger.info("{} Stopped sending updates to user {} with id {}", myLog, username, session.getId());
    }
}