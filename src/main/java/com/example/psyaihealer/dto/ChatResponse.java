package com.example.psyaihealer.dto;

public class ChatResponse {
    private Long sessionId;
    private String reply;
    private String strategy;

    public ChatResponse() {
    }

    public ChatResponse(Long sessionId, String reply, String strategy) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.strategy = strategy;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
