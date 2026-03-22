package com.smarthire.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Message Entity
 * Represents a message sent between HR and Student.
 * HR can send messages, meet links to students and vice versa.
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-incremented primary key

    // Who sent the message
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender; // Sender (HR or Student)

    // Who received the message
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // Receiver (HR or Student)

    @Column(columnDefinition = "TEXT")
    private String content; // Message content

    @Column(name = "meet_link")
    private String meetLink; // Google Meet link (optional)

    @Column(name = "is_read")
    private boolean read = false; // Read status

    @Column(name = "sent_at")
    private LocalDateTime sentAt; // When message was sent

    // Related job (optional - to know which job this message is about)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private Job job; // Related job posting
}