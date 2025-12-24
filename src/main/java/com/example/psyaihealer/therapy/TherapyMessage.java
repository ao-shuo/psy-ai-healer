package com.example.psyaihealer.therapy;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "therapy_messages")
public class TherapyMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private TherapySession session;

    @Enumerated(EnumType.STRING)
    private MessageSender sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    public TherapyMessage() {
    }

    public TherapyMessage(TherapySession session, MessageSender sender, String content) {
        this.session = session;
        this.sender = sender;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public TherapySession getSession() {
        return session;
    }

    public void setSession(TherapySession session) {
        this.session = session;
    }

    public MessageSender getSender() {
        return sender;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
