package com.smarthire.repository;

import com.smarthire.model.Message;
import com.smarthire.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * MessageRepository Interface
 * Handles all database operations related to Messages.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get all messages between two users (conversation)
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findConversation(User user1, User user2);

    // Get all messages received by a user
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);

    // Get all messages sent by a user
    List<Message> findBySenderOrderBySentAtDesc(User sender);

    // Count unread messages for a user
    long countByReceiverAndReadFalse(User receiver);

    // Get all unread messages for a user
    List<Message> findByReceiverAndReadFalse(User receiver);

    // Get all unique senders who messaged this user
    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.receiver = :receiver")
    List<User> findUniqueSenders(User receiver);

    // Get all unique receivers this user messaged
    @Query("SELECT DISTINCT m.receiver FROM Message m WHERE m.sender = :sender")
    List<User> findUniqueReceivers(User sender);
}