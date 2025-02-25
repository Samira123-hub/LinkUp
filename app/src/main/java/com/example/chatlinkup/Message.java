package com.example.chatlinkup;

public class Message {

    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    private boolean isSentByCurrentUser;

    // Constructeur vide nécessaire pour Firebase
    public Message() {}

    public Message(String senderId, String receiverId, String content, long timestamp, boolean isSentByCurrentUser) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.isSentByCurrentUser = isSentByCurrentUser;
    }

    // Getters et setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSentByCurrentUser() {
        return isSentByCurrentUser;
    }

    public void setSentByCurrentUser(boolean sentByCurrentUser) {
        isSentByCurrentUser = sentByCurrentUser;
    }
}
