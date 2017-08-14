package ru.supernacho.chat.server.core;

import ru.supernacho.chat.library.Messages;
import ru.supernacho.chat.network.SocketThread;
import ru.supernacho.chat.network.SocketThreadListener;

import java.net.Socket;

public class ChatSocketThread extends SocketThread {

    private boolean isAuthorized;
    private boolean isReconnected;
    private String nickname;


    ChatSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

    boolean isAuthorized() {
        return isAuthorized;
    }

    boolean isReconnected() {
        return isReconnected;
    }

    String getNickname() {
        return nickname;
    }

    void authAccept(String nickname) {
        this.isAuthorized = true;
        this.nickname = nickname;
        sendMsg(Messages.getAuthAccept(nickname));
    }

    void authError() {
        sendMsg(Messages.getAuthError());
        close();
    }

    void reconnected() {
        isReconnected = true;
        sendMsg(Messages.getReconnect());
        close();
    }

    void messageFormatError(String msg) {
        sendMsg(Messages.getMsgFormatError(msg));
        close();
    }
}
