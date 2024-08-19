package org.devexperts.wsHandlers;

import org.devexperts.service.InstrumentDataService;
import org.devexperts.service.UserService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class InstrumentHandler extends TextWebSocketHandler {
    private final InstrumentDataService instrumentDataService;
    private final UserService userService;


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
        if (payload.startsWith("SUBSCRIBE:")) {
            String symbol = payload.substring(10)
                    .trim();
            instrumentDataService.subscribeToInstrument(
                    username,
                    symbol
            );
            userService.addSubscription(username, symbol);
            session.sendMessage(new TextMessage("Subscribed to " + symbol));
        } else if (payload.startsWith("UNSUBSCRIBE:")) {
            String symbol = payload.substring(12)
                    .trim();
            instrumentDataService.unsubscribeFromInstrument(
                    username,
                    symbol
            );
            userService.removeSubscription(username, symbol);
            session.sendMessage(new TextMessage("Unsubscribed from " + symbol));
        } else {
            session.sendMessage(new TextMessage("Invalid command. Use SUBSCRIBE:symbol or UNSUBSCRIBE:symbol"));
        }
    }
}
