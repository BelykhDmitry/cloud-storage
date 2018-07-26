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
import sun.nio.ch.Net;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable, InputListener {

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
        status.setEditable(false);
        status.setAlignment(Pos.CENTER);
        status.setStyle("-fx-background-color:#f20c0f;-fx-text-fill:#ffffff");
        reg.setDisable(true);
        if(Network.getInstance().getStatus()) {
            status.setText("Connected");
            status.setStyle("-fx-background-color:#40d660;-fx-text-fill:#000000");
            reg.setDisable(false);
        } else {
            new Thread(this::disconnected).start();
            //Platform.runLater(() -> disconnected());
        }
    }

    private void changeScreen() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 400));
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
            disconnected();
        }
    }

    public void connected() {
        status.setText("Connected");
        status.setStyle("-fx-background-color:#40d660;-fx-text-fill:#000000");
        reg.setDisable(false);
    }

    public void disconnected() {
        status.setText("Disconnected");
        status.setStyle("-fx-background-color:#f20c0f;-fx-text-fill:#ffffff");
        reg.setDisable(true);
        try {
            //Network.getInstance().disconnectt();
            while (!Network.getInstance().getStatus()) {
                new Thread(() -> Network.getInstance().connectt()).start();
                Thread.sleep(1000);
            }
            connected();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T extends AbstractMessage> void onMsgReceived(T msg) {
        if (msg instanceof ServerCallbackMessage) {
            Platform.runLater(() -> {
                if (((ServerCallbackMessage) msg).getStatus() == ServerCallbackMessage.Answer.OK) {
                    changeScreen();
                } else if (((ServerCallbackMessage) msg).getStatus() == ServerCallbackMessage.Answer.FAIL){
                    new Alert(Alert.AlertType.ERROR, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
                }
            });
        }
    }
}
