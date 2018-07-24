package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    private boolean authorized = false;

    @FXML
    VBox mainVBox;

    @FXML
    TextField login;

    @FXML
    PasswordField password;
    InputListener listener = new InputListener() {
        @Override
        public <T extends AbstractMessage> void onMsgReceived(T msg) {
            if (msg instanceof ServerCallbackMessage)
                if (((ServerCallbackMessage) msg).getStatus() == ServerCallbackMessage.Answer.OK) {
                    Network.getInstance().removeListener(listener);
                    authorized = true;
                } else {
                    new Alert(Alert.AlertType.CONFIRMATION, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
                }
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(location);
        Network.getInstance().addListener(listener);
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
        System.out.println(login.getText() + " " + password.getText());
        if (Network.getInstance().getStatus())
            Network.getInstance().addToQueue(new AuthMessage(login.getText(), password.getText(), false));
        AbstractMessage msg;
        while(!authorized){
            msg = Network.getInstance().getAnswer();
            if(msg != null) {
                if(msg instanceof ServerCallbackMessage)
                    switch(((ServerCallbackMessage)msg).getStatus()) {
                        case OK:
                            authorized = true;
                            break;
                        case FAIL:
                            new Alert(Alert.AlertType.CONFIRMATION, "Wrong username or password", ButtonType.OK, ButtonType.CANCEL).showAndWait();
                            break;
                    }
                    break;
            }
        }
        if(authorized) changeScreen();
    }
}
