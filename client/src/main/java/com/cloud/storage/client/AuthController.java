package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable, InputListener {

    @FXML
    CheckBox saveUser;

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
        Network.getInstance().addListener(this);
        String username = PropertiesLoader.getInstance().getProperty("username");
        if(username != null)
            login.setText(username);
        status.setEditable(false);
        setUIDisconnected();
        if(Network.getInstance().getStatus()) {
            setUIConnected();
        } else {
            Thread t = new Thread(this::disconnected);
            t.setDaemon(true);
            t.start();
        }
    }

    private void changeScreen() {
        if(saveUser.isSelected()) {
            PropertiesLoader.getInstance().setProperty("username", login.getText());
            try {
                PropertiesLoader.getInstance().store(PropertiesLoader.getInstance().PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 400));
            Network.getInstance().removeListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void auth() {
        System.out.println(login.getText() + " " + password.getText());
        if (Network.getInstance().getStatus()) {
            Network.getInstance().addToQueue(new AuthMessage(login.getText(), password.getText(), registration.isSelected()));
        } else {
            Thread t = new Thread(() -> disconnected());
            t.setDaemon(true);
            t.start();
        }
    }

    private void connected() {
        setUIConnected();
    }

    private void disconnected() {
        Platform.runLater(this::setUIDisconnected);
        try {
            while (!Network.getInstance().getStatus()) {
                Network.getInstance().connect();
                Thread.sleep(5000);
            }
            Platform.runLater(this::connected);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setUIDisconnected() {
        status.setText("Disconnected");
        status.setStyle("-fx-background-radius:4;-fx-background-color:#f20c0f;-fx-text-fill:#ffffff;-fx-border-color: #1d1d1d;-fx-border-radius:4;");
        reg.setDisable(true);
    }

    private void setUIConnected() {
        status.setText("Connected");
        status.setStyle("-fx-background-radius:4;-fx-background-color:#40d660;-fx-text-fill:#000000;-fx-border-color: #1d1d1d;-fx-border-radius: 4;");
        reg.setDisable(false);
    }

    @Override
    public void onMsgReceived(AbstractMessage msg) {
        if (msg instanceof ServerCallbackMessage) {
            Platform.runLater(() -> {
                ServerCallbackMessage message = (ServerCallbackMessage) msg;
                switch (message.getStatus()){
                    case OK:
                        changeScreen();
                        break;
                    case FAIL:
                        new Alert(Alert.AlertType.ERROR, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
                        break;
                    case DISCONNECTED:
                        Thread t = new Thread(this::disconnected);
                        t.setDaemon(true);
                        t.start();
                        break;
                    default:
                            break;
                }
            });
        }
    }
}
