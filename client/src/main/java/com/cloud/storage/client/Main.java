package com.cloud.storage.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/auth.fxml"));
        primaryStage.setTitle("Cloud Storage Client");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> Network.getInstance().removeAll());
    }

    @Override
    public void init() throws Exception {
        super.init();
        Properties prop = new Properties();
        String host = null;
        int port;
        try {
            prop.load(new FileInputStream("/prop.dtd"));
            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.err.println("Не найден файл настроек/файл настроек неполный");
            host = "localhost";
            port = 8189;
        }
        try {
            Network.getInstance().connect(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Не удалось подключиться к серверу");
        }
    }

    @Override
    public void stop() throws Exception {
        Network.getInstance().disconnect();
//        Properties prop = new Properties();
//        prop.setProperty("host", "localhost");
//        prop.setProperty("port", "8189");
//        prop.store(new BufferedOutputStream(new FileOutputStream("prop.dtd")), "Server parameters");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
