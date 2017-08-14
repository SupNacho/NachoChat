package ru.supernacho.chat.server.core;

public interface ChatServerListener {
    void onLogChatServer(ChatServer chatServer, String msg);
}
