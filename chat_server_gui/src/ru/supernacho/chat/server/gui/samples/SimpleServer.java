package ru.supernacho.chat.server.gui.samples;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             Socket socket = serverSocket.accept()
        ){
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                out.writeUTF("echo: " + in.readUTF());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
