package ru.supernacho.chat.network;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListener {

    void onStartServerSocketThread(ServerSocketThread thread);
    void onStopServerSocketThread(ServerSocketThread thread);

    void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket);
    void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket);
    void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket);

    void onExceptionServerSocketThread(ServerSocketThread thread, Exception e);

}
