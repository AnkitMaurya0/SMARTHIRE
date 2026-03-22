package com.smarthire.service;

import com.smarthire.model.Message;
import com.smarthire.model.Job;
import com.smarthire.model.User;
import com.smarthire.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MessageService Class
 * Handles all business logic related to Messages.
 * Called by MessageController.
 */
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository; // database operations

    /**
     * Send a new message from sender to receiver
     */
    public Message sendMessage(User sender, User receiver,
            String content, String meetLink, Job job) {

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setMeetLink(meetLink);
        message.setJob(job);
        message.setRead(false);
        message.setSentAt(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Get full conversation between two users
     */
    public List<Message> getConversation(User user1, User user2) {
        return messageRepository.findConversation(user1, user2);
    }

    /**
     * Get all inbox messages for a user
     */
    public List<Message> getInbox(User user) {
        return messageRepository.findByReceiverOrderBySentAtDesc(user);
    }

    /**
     * Count unread messages for a user
     * Shown as notification count on dashboard
     */
    public long getUnreadCount(User user) {
        return messageRepository.countByReceiverAndReadFalse(user);
    }

    /**
     * Mark all messages from a sender as read
     */
    public void markAsRead(User receiver, User sender) {
        List<Message> messages = messageRepository
                .findConversation(receiver, sender);

        for (Message message : messages) {
            if (message.getReceiver().equals(receiver) && !message.isRead()) {
                message.setRead(true);
                messageRepository.save(message);
            }
        }
    }

    /**
     * Get all people this user has conversation with
     * Used to show contact list in inbox
     */
    public List<User> getContactList(User user) {
        List<User> contacts = new ArrayList<>();

        // People who sent messages to this user
        List<User> senders = messageRepository.findUniqueSenders(user);

        // People this user sent messages to
        List<User> receivers = messageRepository.findUniqueReceivers(user);

        // Combine and remove duplicates
        contacts.addAll(senders);
        for (User r : receivers) {
            if (!contacts.contains(r)) {
                contacts.add(r);
            }
        }

        return contacts;
    }

    /**
     * Get message by ID
     */
    public Message getById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }
}