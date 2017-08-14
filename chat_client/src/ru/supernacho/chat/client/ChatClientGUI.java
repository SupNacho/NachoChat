package ru.supernacho.chat.client;

import ru.supernacho.chat.library.DefaultGUIExceptionHandler;
import ru.supernacho.chat.library.Messages;
import ru.supernacho.chat.network.SocketThread;
import ru.supernacho.chat.network.SocketThreadListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ChatClientGUI extends JFrame implements ActionListener, SocketThreadListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private static final int WIDTH = 800;
    private static final int HEIGHT = 300;
    private static final String TITLE = "SuperNacho chat";
    private static final String EMPTYLIST[] = {"Disconnected"};

    private static String Target = Messages.MSG_TO_ALL_USER;

    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
    private final JTextField fieldIPAddr = new JTextField("127.0.0.1"); //89.222.249.131
    private final JTextField fieldPort = new JTextField("8189");
    private final JCheckBox chkAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField fieldLogin = new JTextField("1");
    private final JPasswordField fieldPass = new JPasswordField("1");
    private final JButton btnLogin = new JButton("Login");

    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();

    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField fieldInput = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
    private StringBuilder stringBuilder = new StringBuilder();

    private ChatClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(new DefaultGUIExceptionHandler());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        upperPanel.add(fieldIPAddr);
        upperPanel.add(fieldPort);
        upperPanel.add(chkAlwaysOnTop);
        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPass);
        upperPanel.add(btnLogin);
        add(upperPanel, BorderLayout.NORTH);

        JScrollPane scrollLog = new JScrollPane(log);
        log.setEditable(false);
        add(scrollLog, BorderLayout.CENTER);

        JScrollPane scrollUsers = new JScrollPane(userList);
        scrollUsers.setPreferredSize(new Dimension(150, 0));
        add(scrollUsers, BorderLayout.EAST);

        bottomPanel.add(btnDisconnect, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.setVisible(false);
        add(bottomPanel, BorderLayout.SOUTH);

        fieldIPAddr.addActionListener(this);
        fieldPort.addActionListener(this);
        fieldLogin.addActionListener(this);
        fieldPass.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        fieldInput.addActionListener(this);
        btnSend.addActionListener(this);
        chkAlwaysOnTop.addActionListener(this);

        setAlwaysOnTop(chkAlwaysOnTop.isSelected());
        userList.setListData(EMPTYLIST);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean a = true;
        Object src = e.getSource();
        if (    src == fieldIPAddr ||
                src == fieldPort   ||
                src == fieldLogin  ||
                src == fieldPass   ||
                src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            disconnect();
        } else if (src == fieldInput || src == btnSend) {
            sendMsg();
        } else if (src == chkAlwaysOnTop) {
            setAlwaysOnTop(chkAlwaysOnTop.isSelected());
        } else {
            throw new RuntimeException("Unknown src = " + src);
        }
    }

    private SocketThread socketThread;

    private void connect() {
        try {
            Socket socket = new Socket(fieldIPAddr.getText(), Integer.parseInt(fieldPort.getText()));
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception: " + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    private void sendMsg() {
        final String msg = fieldInput.getText();
//        String msgToSend = "";
        final String selectedUser = userList.getSelectedValue();
        stringBuilder.delete(0, stringBuilder.capacity());
        if (selectedUser == null) {
            stringBuilder.append(Messages.MSG_TO_ALL_USER);
            stringBuilder.append(Messages.DELIMITER);
            stringBuilder.append(msg);
//            msgToSend = Messages.MSG_TO_ALL_USER + Messages.DELIMITER + msg;
        } else if(msg.equals("")) {
            return;
        } else if (selectedUser != null) {
            stringBuilder.append(selectedUser);
            stringBuilder.append(Messages.DELIMITER);
            stringBuilder.append(msg);
//            msgToSend = selectedUser + Messages.DELIMITER + msg;
        }
        fieldInput.setText(null);
        fieldInput.requestFocus();
        socketThread.sendMsg(stringBuilder.toString());
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Поток сокета запущен.\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Соединение потеряно.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(true);
                bottomPanel.setVisible(false);
                userList.setListData(EMPTYLIST);
                setTitle(TITLE);
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Соединение установлено.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(false);
                bottomPanel.setVisible(true);
                String login = fieldLogin.getText();
                String password = new String(fieldPass.getPassword());
                socketThread.sendMsg(Messages.getAuthRequest(login, password));
            }
        });
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String tokens[] = value.split(Messages.DELIMITER);
                switch (tokens[0]) {
                    case Messages.BROADCAST :
                        log.append(simpleDateFormat.format(Long.parseLong(tokens[1])) + " - [ ВСЕМ ] -> " + tokens[2] +
                                ": " + tokens[3] + "\n");
                        break;
                    case Messages.USERS_LIST :
                        int msgHeaderCut = Messages.USERS_LIST.length() + Messages.DELIMITER.length();
                        String users[] = value.substring(msgHeaderCut).split(Messages.DELIMITER);
                        Arrays.sort(users);
                        userList.setListData(users);
                        break;
                    case Messages.AUTH_ACCEPT :
                        setTitle(TITLE + " >> " + tokens[1]);
                        log.append(simpleDateFormat.format(System.currentTimeMillis()) +
                                " - [ Успешная авторизация ] -> " + tokens[1] + "\n");
                        break;
                    case Messages.AUTH_ERROR :
                        log.append(simpleDateFormat.format(System.currentTimeMillis()) + " - [ Ошибка авторизации ]\n");
                        break;
                    case Messages.MSG_FORMAT_ERROR :
                        log.append(simpleDateFormat.format(System.currentTimeMillis()) +
                                " - [Ошибка формата сообщения] - > " + tokens[1] + "\n");
                        break;
                    case Messages.RECONNECT :
                        log.append(simpleDateFormat.format(System.currentTimeMillis()) +
                                " - [Переподключение с другого устройства] \n");
                        break;
                    case  Messages.PRIVATE :
                        log.append(simpleDateFormat.format(Long.parseLong(tokens[1])) + " - [ ПРИВАТ ] -> " + tokens[2]
                                + ": " + tokens[3] + "\n");
                        break;
                    default:
                        throw new RuntimeException("Неизвестный заголовок сообщения");
                }
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
                log.append("Exception: " + e.getMessage() + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
