package ru.supernacho.chat.server.core;

public interface AuthService {
    void start();
    String getNickname(String login, String password);
    void stop();
}
