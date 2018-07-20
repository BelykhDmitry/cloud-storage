package com.cloud.storage.server.Functions;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import com.sun.istack.internal.NotNull;
import io.netty.handler.codec.serialization.ClassResolvers;

import java.sql.*;

public class Authorization {
    private static volatile Authorization instance;

    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;

    public static Authorization getInstance() {
        Authorization localInstance = instance;
        if (localInstance == null) {
            synchronized (Authorization.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Authorization();
                }
            }
        }
        return localInstance;
    }

    @NotNull
    public boolean authorize (AuthMessage msg) {
        boolean result = false;
        try {
            ResultSet resultSet = statement.executeQuery("SELECT Pas FROM Users WHERE Name LIKE '"+ msg.getName() +"';");
            if (resultSet.next()) {
                result = msg.getPass().equals(resultSet.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean register (AuthMessage msg) {
        boolean result = false;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO Users (Name, Pas) VALUES ('"+msg.getName()+"','"+msg.getPass()+"');");
            preparedStatement.execute();
            connection.commit();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:auth_db.db"); //TODO: Реализация интерфейса для легкого перехода от одного типа БД к другому. Требуется база данных с паролем
        connection.setAutoCommit(false);
        statement = connection.createStatement();
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
