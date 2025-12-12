package org.example.bankingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AdminMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "ALL" for broadcast, or Account Number for individual
    private String recipient;

    @Column(length = 2000)
    private String content;

    private LocalDateTime sentAt = LocalDateTime.now();

    private String type; // "INDIVIDUAL" or "BROADCAST"

    public AdminMessage() {
    }

    public AdminMessage(String recipient, String content, String type) {
        this.recipient = recipient;
        this.content = content;
        this.type = type;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
