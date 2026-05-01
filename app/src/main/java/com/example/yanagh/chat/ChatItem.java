package com.example.yanagh.chat;

import java.util.Objects;


public class ChatItem {
    private final String id;
    private final String role;
    private final String content;

    public ChatItem(String id, String role, String content) {
        this.id = id;
        this.role = role;
        this.content = content;
    }

    public String getId() { return id; }
    public String getRole() { return role; }
    public String getContent() { return content; }

    public boolean isUser() {
        return "user".equals(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatItem chatItem = (ChatItem) o;
        return Objects.equals(id, chatItem.id) &&
                Objects.equals(role, chatItem.role) &&
                Objects.equals(content, chatItem.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, content);
    }
}
