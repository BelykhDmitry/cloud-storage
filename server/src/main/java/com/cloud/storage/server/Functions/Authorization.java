package com.cloud.storage.server.Functions;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import com.sun.istack.internal.NotNull;
import io.netty.handler.codec.serialization.ClassResolvers;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Authorization {

    private static Logger log = Logger.getLogger(Authorization.class.getName());

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
                result = msg.getPass() == resultSet.getString(1).hashCode();
            }
            log.info(msg.getName() + " authorization " + result);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
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
            log.info("New user registered: " + msg.getName());
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
        }
        return result;
    }

    public boolean changePass (String user, CmdMessage msg) {
        boolean result = false;
        try {
            preparedStatement = connection.prepareStatement("UPDATE Users SET Pas='"+msg.getCmd()+"' WHERE Name LIKE '"+user+"';");
            preparedStatement.execute();
            connection.commit();
            result = true;
            log.info("User " + user + " changed pass");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
        }
        return result;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:auth_db.db"); //TODO: Реализация интерфейса для легкого перехода от одного типа БД к другому. Требуется база данных с паролем
        connection.setAutoCommit(false);
        statement = connection.createStatement();
        log.info("DataBase connect OK");
    }

    public void disconnect() {
        try {
            statement.close();
            connection.close();
            log.info("DataBase disconnect OK");
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
        }
    }
}
