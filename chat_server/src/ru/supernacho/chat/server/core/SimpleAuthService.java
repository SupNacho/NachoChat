package ru.supernacho.chat.server.core;

import java.util.ArrayList;

public class SimpleAuthService implements AuthService {

    private static class Entrys {

        private final String login;
        private final String password;
        private final String nick;

        public Entrys(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }

        private boolean isMe (String login, String password){
            return this.login.equals(login) && this.password.equals(password);
        }
    }

    private final ArrayList<Entrys> users = new ArrayList<>();

    @Override
    public void start() {
        users.add(new Entrys("3","3","Бендер Сгибальщик"));
        users.add(new Entrys("1","1","Фендер Стратокастер"));
        users.add(new Entrys("2","2","Лила"));
        users.add(new Entrys("0","0","Фрай"));
    }

    @Override
    public String getNickname(String login, String password) {
        for (int i = 0; i < users.size(); i++) {
            Entrys entry = users.get(i);
            if (entry.isMe(login,password)) return entry.nick;
        }
        return null;
    }

    @Override
    public void stop() {

    }
}
