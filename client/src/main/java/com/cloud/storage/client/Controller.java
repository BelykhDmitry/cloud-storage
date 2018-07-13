package com.cloud.storage.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class Controller {
    @FXML
    TextArea textArea;

    public void btnClickMeAction() {
        System.out.println("Java");
    }

    public void btn2Click() {
        textArea.appendText("Java\n");
    }
}
