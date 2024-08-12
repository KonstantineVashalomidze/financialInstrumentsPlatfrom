package org.devexperts.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    @Indexed
    private String senderUsername;

    @Indexed
    private String receiverUsername;

    private String content;

    @CreatedDate
    @Indexed
    private Date timestamp;

    @DBRef
    private User sender;

    @DBRef
    private User receiver;

    // Constructor using usernames
    public Message(String senderUsername, String receiverUsername, String content) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.content = content;
    }

    // No-args constructor (required by Spring Data)
    protected Message() {
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
        this.senderUsername = sender.getUsername();
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
        this.receiverUsername = receiver.getUsername();
    }
}