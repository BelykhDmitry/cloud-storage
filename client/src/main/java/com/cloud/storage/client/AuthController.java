package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(location);
    }

    private void changeScreen() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/mainWindow.fxml"));
            ((Stage) mainVBox.getScene().getWindow()).setScene(new Scene(root, 600, 400));
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
        AbstractMessage msg;
        while(!serverCallBack) {}
        if (authorized) {
            changeScreen();
        } else {
            new Alert(Alert.AlertType.CONFIRMATION, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
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
