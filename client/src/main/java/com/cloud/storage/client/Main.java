package com.cloud.storage.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
        Network.getInstance().connect("localhost", 8189);
    }

    @Override
    public void stop() throws Exception {
        Network.getInstance().disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
