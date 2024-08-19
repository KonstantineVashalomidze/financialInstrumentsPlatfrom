package org.devexperts.wsHandlers;

import org.devexperts.model.Message;
import org.devexperts.service.MessageService;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler
        extends TextWebSocketHandler {

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

        if (sessions.containsKey(username)) {
            sessions.get(username).close();
        }

        sessions.put(
                username,
                session
        );

        session.sendMessage(new TextMessage("Connected successfully. You can now send messages."));
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

        if (sessions.containsKey(username)) {
            sessions.remove(username);
        }
    }


}