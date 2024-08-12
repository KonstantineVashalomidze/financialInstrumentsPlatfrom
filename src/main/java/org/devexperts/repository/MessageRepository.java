package org.devexperts.repository;

import org.devexperts.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    // Find messages by sender username
    @Query("{ 'senderUsername': ?0 }")
    List<Message> findBySenderUsername(String senderUsername);

    // Find messages by receiver username
    @Query("{ 'receiverUsername': ?0 }")
    List<Message> findByReceiverUsername(String receiverUsername);

    // Find messages between two users
    @Query("{ $or: [ { 'senderUsername': ?0, 'receiverUsername': ?1 }, { 'senderUsername': ?1, 'receiverUsername': ?0 } ] }")
    List<Message> findMessagesBetweenUsers(String username1, String username2);

    // Find messages by sender username within a date range
    @Query("{ 'senderUsername': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Message> findBySenderUsernameAndTimestampBetween(String senderUsername, Date start, Date end);

    // Find messages by receiver username within a date range
    @Query("{ 'receiverUsername': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Message> findByReceiverUsernameAndTimestampBetween(String receiverUsername, Date start, Date end);

    // Find latest messages for a user (either as sender or receiver)
    @Query("{ $or: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?0 } ] }")
    List<Message> findLatestMessagesForUser(String username, org.springframework.data.domain.Pageable pageable);

    // Find messages containing specific content (case-insensitive)
    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    List<Message> findByContentContainingIgnoreCase(String content);

    // Delete all messages older than a certain date
    void deleteByTimestampBefore(Date date);
}
