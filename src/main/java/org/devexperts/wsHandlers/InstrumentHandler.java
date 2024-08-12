package org.devexperts.wsHandlers;

import org.devexperts.service.InstrumentDataService;
import org.devexperts.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class InstrumentHandler extends TextWebSocketHandler {
    private final InstrumentDataService instrumentDataService;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(InstrumentHandler.class);
    private static final String myLog = "[!!!MY_LOG!!!]";

    public InstrumentHandler(InstrumentDataService instrumentDataService, UserService userService) {
        this.instrumentDataService = instrumentDataService;
        this.userService = userService;
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    )
            throws
            Exception {
        String username = (String) session.getAttributes()
                .get("username");
        String payload = message.getPayload();
        logger.info("{} Received subscription message {} from user {}", myLog, payload, username);
        if (payload.startsWith("SUBSCRIBE:")) {
            String symbol = payload.substring(10)
                    .trim();
            instrumentDataService.subscribeToInstrument(
                    username,
                    symbol
            );
            logger.info("{} User {} subscribed instrument {}", myLog, username, payload);
            userService.addSubscription(username, symbol);
            logger.info("{} User subscriptions for user {} got new instrument {}", myLog, username, payload);
            session.sendMessage(new TextMessage("Subscribed to " + symbol));
        } else if (payload.startsWith("UNSUBSCRIBE:")) {
            String symbol = payload.substring(12)
                    .trim();
            instrumentDataService.unsubscribeFromInstrument(
                    username,
                    symbol
            );
            logger.info("{} User {} unsubscribed instrument {}", myLog, username, payload);
            userService.removeSubscription(username, symbol);
            logger.info("{} User subscriptions for user {} lost instrument {}", myLog, username, payload);
            session.sendMessage(new TextMessage("Unsubscribed from " + symbol));
        } else {
            session.sendMessage(new TextMessage("Invalid command. Use SUBSCRIBE:symbol or UNSUBSCRIBE:symbol"));
            logger.info("{} User {} sent unknown subscription {}", myLog, username, payload);
        }
    }
}
