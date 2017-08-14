package ru.supernacho.chat.library;

public class Messages {

    public static final String DELIMITER =          ";";
    public static final String AUTH_REQUEST =       "/auth_request";
    public static final String AUTH_ACCEPT =        "/auth_accept";
    public static final String AUTH_ERROR =         "/auth_error";
    public static final String USERS_LIST =         "/user_list";
    public static final String RECONNECT =          "/reconnect";
    public static final String BROADCAST =          "/bcast";
    public static final String PRIVATE =            "/private";
    public static final String MSG_FORMAT_ERROR =   "/msg_format_error";
    public static final String MSG_TO_ALL_USER =   "> ВСЕМ";

    // /auth_request login password
    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    // /auth_accept nick
    public static String getAuthAccept(String nick){
        return AUTH_ACCEPT + DELIMITER + nick;
    }

    // BROADCAST time src message
    public static String getBroadcast(String src, String value){
        return BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + src + DELIMITER + value;
    }

    // PPRIVATE time src message
    public static String getPrivate(String src, String value){
        return PRIVATE + DELIMITER + System.currentTimeMillis() + DELIMITER + src + DELIMITER + value;
    }

    // /user_list time user1 user2 user3
    public static String getUsersList(String users){
        return USERS_LIST + DELIMITER + users;
    }

    // AUTH_ERROR time message
    public static String getAuthError(){
        return AUTH_ERROR;
    }

    public static String getReconnect(){
        return RECONNECT;
    }

    // /msg_format_error time value
    public static String getMsgFormatError(String value){
        return MSG_FORMAT_ERROR + DELIMITER + value;
    }
}
