package org.devexperts.repository;

import org.devexperts.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    @Query("{ 'senderUsername': ?0 }")
    List<Message> findBySenderUsername(String senderUsername);

    @Query("{ 'receiverUsername': ?0 }")
    List<Message> findByReceiverUsername(String receiverUsername);

    @Query("{ $or: [ { 'senderUsername': ?0, 'receiverUsername': ?1 }, { 'senderUsername': ?1, 'receiverUsername': ?0 } ] }")
    List<Message> findMessagesBetweenUsers(String username1, String username2);

    @Query("{ 'senderUsername': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Message> findBySenderUsernameAndTimestampBetween(String senderUsername, Date start, Date end);

    @Query("{ 'receiverUsername': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    List<Message> findByReceiverUsernameAndTimestampBetween(String receiverUsername, Date start, Date end);

    @Query("{ $or: [ { 'senderUsername': ?0 }, { 'receiverUsername': ?0 } ] }")
    List<Message> findLatestMessagesForUser(String username, org.springframework.data.domain.Pageable pageable);

    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    List<Message> findByContentContainingIgnoreCase(String content);

    void deleteByTimestampBefore(Date date);
}
