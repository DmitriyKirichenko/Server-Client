package com.kirichenko.Server.db;

import com.kirichenko.Server.entity.Config;
import com.kirichenko.Server.entity.Message;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

public class DbFactory {
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        } else {
            try {
                Class.forName(Config.DRIVER);
                connection = DriverManager.getConnection(Config.URL, Config.USER, Config.PASSWORD);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
            return connection;
        }
    }

    public static void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {  /*NOP*/ }
    }

    public static void saveMessage(Message message) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement("insert into history (date, message) values (?, ?)");
        preparedStatement.setTimestamp(1, new Timestamp(message.getDate().getTime()));
        preparedStatement.setString(2, message.getMessage());
        preparedStatement.execute();
    }
    public static List <Message> getMessages () throws SQLException {
        Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement("select * from history order by date desc limit 10");
        ResultSet resultSet = preparedStatement.executeQuery();

        List<Message> list= new LinkedList<>();

        while (resultSet.next()){
            Date date = new Date(resultSet.getTimestamp("date").getTime());
            String messageText = resultSet.getString("message");
            Message message = new Message(messageText, date);
            list.add(0, message);
        }
        return list;
    }
}
