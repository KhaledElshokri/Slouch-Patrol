package com.example.slouch_patrol_app.Features;

public class Message {
    private String content;
    private boolean isUser; // True if the message is from the user

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }
}

