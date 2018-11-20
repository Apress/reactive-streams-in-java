package com.github.adamldavis.akkahttp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ChatMessage {

    final String username;
    final String message;

    @JsonCreator
    public ChatMessage(@JsonProperty("username") String username,
                       @JsonProperty("message") String message) {
        this.username = username;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ChatMessage {username='" + username + '\'' +
                ", message='" + message + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, message);
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
