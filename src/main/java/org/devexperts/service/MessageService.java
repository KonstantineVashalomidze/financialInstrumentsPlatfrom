package org.devexperts.service;

import org.devexperts.model.Message;
import org.devexperts.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }

    public Optional<Message> getMessageById(String id) {
        return messageRepository.findById(id);
    }

    public List<Message> getMessagesBySender(String senderUsername) {
        return messageRepository.findBySenderUsername(senderUsername);
    }

    public List<Message> getMessagesByReceiver(String receiverUsername) {
        return messageRepository.findByReceiverUsername(receiverUsername);
    }

    public List<Message> getMessagesBetweenUsers(String username1, String username2) {
        return messageRepository.findMessagesBetweenUsers(username1, username2);
    }

    public List<Message> getMessagesBySenderInDateRange(String senderUsername, Date start, Date end) {
        return messageRepository.findBySenderUsernameAndTimestampBetween(senderUsername, start, end);
    }

    public List<Message> getMessagesByReceiverInDateRange(String receiverUsername, Date start, Date end) {
        return messageRepository.findByReceiverUsernameAndTimestampBetween(receiverUsername, start, end);
    }

    public List<Message> getLatestMessagesForUser(String username, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return messageRepository.findLatestMessagesForUser(username, pageable);
    }

    public List<Message> searchMessages(String content) {
        return messageRepository.findByContentContainingIgnoreCase(content);
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
    }

    public void deleteOldMessages(Date date) {
        messageRepository.deleteByTimestampBefore(date);
    }
}
