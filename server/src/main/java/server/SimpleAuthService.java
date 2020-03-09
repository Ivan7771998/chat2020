package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chat_database.db");
        stmt = connection.createStatement();
    }

    public static void prepareInsertRecord() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO Users (login, password, nickname) VALUES (?,?,?);");
    }

    public static void updateNick(String login, String nickname) throws SQLException {
        stmt.executeUpdate("UPDATE Users SET nickname = '" + nickname + "' WHERE nickname = '" + login + "'");
    }

    public static void addUser(String login, String password, String nickname) throws SQLException {
        connection.setAutoCommit(false);
        psInsert.setString(1, login);
        psInsert.setString(2, password);
        psInsert.setString(3, nickname);
        psInsert.executeUpdate();
        connection.setAutoCommit(true);
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SimpleAuthService() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT nickname,login, password FROM Users");
        String nickname = null;
        while (rs.next()) {
            if (rs.getString("login").equals(login) &&
                    rs.getString("password").equals(password)) {
                nickname = rs.getString("nickname");
            }
        }
        rs.close();
        return nickname;
    }

    @Override
    public boolean registration(String login, String password, String nickname) throws SQLException {
        boolean status = true;
        ResultSet rs = stmt.executeQuery("SELECT login FROM Users");
        while (rs.next()) {
            if (rs.getString("login").equals(login)) {
                status = false;
                break;
            }
        }

        if (!status || password.trim().equals("")) {
            return false;
        }

        prepareInsertRecord();
        addUser(login, password, nickname);
        return true;
    }

    @Override
    public void changeNickName(String login, String nickname) throws SQLException {
        updateNick(login, nickname);
    }
}
