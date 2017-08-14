package ru.supernacho.chat.server.core;

import ru.supernacho.chat.library.Messages;
import ru.supernacho.chat.network.ServerSocketThread;
import ru.supernacho.chat.network.ServerSocketThreadListener;
import ru.supernacho.chat.network.SocketThread;
import ru.supernacho.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final AuthService authService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final ChatServerListener eventListener;
    private ServerSocketThread serverSocketThread;
    private final Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener eventListener, AuthService authService) {
        this.eventListener = eventListener;
        this.authService = authService;
    }

    public void startListening(int port) {
        if(serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Поток сервера уже запущен.");
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 2000);
        authService.start();
    }

    public synchronized void dropAllClients() {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
        putLog("dropAllClients");
    }

    public void stopListening() {
        if(serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Поток сервера не запущен.");
            return;
        }
        serverSocketThread.interrupt();
    }

    private synchronized void putLog(String msg) {
        String msgLog = dateFormat.format(System.currentTimeMillis());
        msgLog += Thread.currentThread().getName() + ": " + msg;
        eventListener.onLogChatServer(this, msgLog);
    }


    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("stopped.");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("ServerSocket is ready...");
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("accept() timeout");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ":" + socket.getPort();
        new ChatSocketThread(this, threadName, socket);
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }


    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        putLog("stopped.");
        clients.remove(socketThread);
        ChatSocketThread client = (ChatSocketThread) socketThread;
        if ( client.isAuthorized() && !client.isReconnected()) {
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNickname() + " disconnected."));
            sendToAllAuthorizedClients(Messages.getUsersList(getAllUsersNicks()));
        }

    }

    private ChatSocketThread getClientByNickname(String nickname) {
        final  int cnt = clients.size();
        for (int i = 0; i < cnt; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname)) return client;
        }
        return null;
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready.");
        clients.add(socketThread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String value) {

        ChatSocketThread client = (ChatSocketThread) socketThread;

        if ( client.isAuthorized()) {
            handleAuthorizeClient(client, value);
        } else {
            handleNonAuthorizeClient(client, value);
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if(client.isAuthorized()) client.sendMsg(msg);
        }
    }

    private void sendPrivateToUser(String recipient, String sender, String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if(client.isAuthorized() && client.getNickname().equals(recipient)) client.sendMsg(msg);
            if(client.isAuthorized() && client.getNickname().equals(sender)) client.sendMsg(msg);
        }
    }

    private void handleAuthorizeClient(ChatSocketThread client, String msg) {
        String[] msgArr = msg.split(Messages.DELIMITER);
        if (msgArr[0].equals(Messages.MSG_TO_ALL_USER) || msgArr[0].isEmpty()) {
            sendToAllAuthorizedClients(Messages.getBroadcast(client.getNickname(), msgArr[1]));
        } else {
            sendPrivateToUser(msgArr[0], client.getNickname(), Messages.getPrivate(client.getNickname(), msgArr[1]));
        }

    }

    private void handleNonAuthorizeClient(ChatSocketThread newClient, String msg) {
        String tokens[] = msg.split(Messages.DELIMITER);
        if(tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)){
            newClient.messageFormatError(msg);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = authService.getNickname(login, password);
        if (nickname == null) {
            newClient.authError();
            return;
        }

        ChatSocketThread oldClient = getClientByNickname(nickname);
        newClient.authAccept(nickname);
        if (oldClient == null) {
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", newClient.getNickname() + " connected."));

        } else {
            oldClient.reconnected();
        }
        sendToAllAuthorizedClients(Messages.getUsersList(getAllUsersNicks()));
    }

    private String getAllUsersNicks() {
        final int cnt = clients.size();
        final int last = cnt - 1;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Messages.MSG_TO_ALL_USER);
        stringBuilder.append(Messages.DELIMITER);
        for (int i = 0; i < cnt ; i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if(!client.isAuthorized() || client.isReconnected()) continue;
            stringBuilder.append(client.getNickname());
            if (i != last) stringBuilder.append(Messages.DELIMITER);
        }
        return stringBuilder.toString();
    }


    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }
}
