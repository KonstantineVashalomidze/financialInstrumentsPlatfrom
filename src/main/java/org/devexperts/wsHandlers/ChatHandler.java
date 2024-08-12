package org.devexperts.wsHandlers;

import org.devexperts.controller.AuthController;
import org.devexperts.model.Message;
import org.devexperts.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler
        extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatHandler.class);
    private static final String myLog = "[!!!MY_LOG!!!]";
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final MessageService messageService;

    public ChatHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws
            Exception {
        String username = (String) session.getAttributes()
                .get("username");
        logger.info("{} Connection established with user {}", myLog, username);

        if (sessions.containsKey(username)) {
            sessions.get(username).close();
            logger.info("{} Another session {} of user {} was closed", myLog, sessions.get(username).getId(), username);
        }

        sessions.put(
                username,
                session
        );

        logger.info("{} Created new session {} for user {}", myLog, session.getId(), username);
        // Notify the user of successful connection
        session.sendMessage(new TextMessage("Connected successfully. You can now send messages."));
        logger.info("{} User {} connected", myLog, username);
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    )
            throws
            Exception {
        String senderUsername = (String) session.getAttributes()
                .get("username");
        String payload = message.getPayload();
        logger.info("{} Received text message {} from {}", myLog, payload, senderUsername);

        String[] parts = payload.split( // Message should have format <recipient>:<message>
                ":",
                2
        );

        if (parts.length == 2) { // Message has specified format
            String recipientUsername = parts[0].trim();
            String messageContent = parts[1].trim();

            WebSocketSession recipientSession = sessions.get(recipientUsername);
            if (recipientSession != null && recipientSession.isOpen()) { // if recipient username found, and is connected to server.
                recipientSession.sendMessage(new TextMessage("From " + senderUsername + ": " + messageContent));
                logger.info("{} Sent text message {} to {}", myLog, messageContent, recipientUsername);
                messageService.createMessage(new Message(senderUsername, recipientUsername, messageContent));
                session.sendMessage(new TextMessage("Message sent to " + recipientUsername));
            } else {
                session.sendMessage(new TextMessage("User " + recipientUsername + " is not available"));
            }
        } else {
            session.sendMessage(new TextMessage("Invalid message format. Please use 'RECIPIENT:MESSAGE'"));
        }


    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        String username = (String) session.getAttributes()
                .get("username");
        logger.info("{} Connection closed with user {}", myLog, username);

        if (sessions.containsKey(username)) {
            sessions.remove(username);
            logger.info("{} Session {} of user {} was closed", myLog, session.getId(), username);
        }
    }


}