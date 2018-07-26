package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable, InputListener {

    private volatile boolean authorized = false;
    private volatile boolean serverCallBack = false;

    @FXML
    VBox mainVBox;

    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    CheckBox registration;

    @FXML
    TextField status;

    @FXML
    Button reg;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(location);
        status.setEditable(false);
        status.setAlignment(Pos.CENTER);
        if(!Network.getInstance().getStatus()) {
            status.setText("Disconnected");
            reg.setDisable(true);
            Thread auth = new Thread(() -> {
                while(!Network.getInstance().getStatus()) {
                    try {
                        String host = "localhost";
                        int port = 8189;
                        System.err.println("Подключение к " + host + ":" + port);
                        Network.getInstance().connect(host, port);
                        Thread.sleep(1000);
                    } catch (IOException e) {
                        System.err.println("Неудачная попытка подключения");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(() -> {
                    status.setText("Connected");
                    status.setStyle("-fx-background-color:#40d660;-fx-text-fill:#000000");
                    reg.setDisable(false);
                });
            });
            auth.setDaemon(true);
            auth.start();
        } else {
            status.setText("Connected");
            status.setStyle("-fx-background-color:#40d660;-fx-text-fill:#000000");
        }
    }

    private void changeScreen() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth() {
        serverCallBack = false;
        authorized = false;
        Network.getInstance().addListener(this);
        System.out.println(login.getText() + " " + password.getText());
        if (Network.getInstance().getStatus())
            Network.getInstance().addToQueue(new AuthMessage(login.getText(), password.getText(), registration.isSelected()));
        while(!serverCallBack) {}
        if (authorized) {
            changeScreen();
        } else {
            new Alert(Alert.AlertType.ERROR, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
        }
        Network.getInstance().removeListener(this);
    }

    @Override
    public <T extends AbstractMessage> void onMsgReceived(T msg) {
        if (msg instanceof ServerCallbackMessage) {
            if (((ServerCallbackMessage) msg).getStatus() == ServerCallbackMessage.Answer.OK) {
                authorized = true;
            }
            serverCallBack = true;
        }
    }
}
