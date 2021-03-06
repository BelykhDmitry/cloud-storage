package com.cloud.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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
        try {
            PropertiesLoader.getInstance().load(PropertiesLoader.getInstance().PATH);
            System.err.println(PropertiesLoader.getInstance().getProperty("host") + ":" + PropertiesLoader.getInstance().getProperty("port"));
        } catch (IOException e){
            System.err.println("Не удалось загрузить настройки: " + e.getMessage());
        }
        super.init();
        Network.getInstance().start();
        Network.getInstance().connect();
    }

    @Override
    public void stop() throws Exception {
        Network.getInstance().disconnect();
        Network.getInstance().stop();
        System.out.println("I'm done!");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
