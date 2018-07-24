package com.cloud.storage.client;

import com.cloud.storage.common.FileMessage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    VBox mainVBox;

    @FXML
    TreeTableView<FileMessage> pathView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Init");
        initPathView();
    }

    private void initPathView() {
        //Запрос на сервак
        FileMessage msg = new FileMessage("/", true, null, 0);
        TreeItem<FileMessage> root = new TreeItem<>(msg);
        pathView.setRoot(root);
        pathView.setShowRoot(false);
        TreeTableColumn<FileMessage, String> nameColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileMessage, Boolean> isDirectoryColumn = new TreeTableColumn<>("Is Directory");
        TreeTableColumn<FileMessage, String> sizeColumn = new TreeTableColumn<>("Size");
        nameColumn.setEditable(true);
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getRelativeNameProperty());
        isDirectoryColumn.setCellValueFactory(param -> param.getValue().getValue().getIsDirectoryProperty());
        sizeColumn.setCellValueFactory(param -> param.getValue().getValue().getSizeProperty());
        pathView.getColumns().setAll(nameColumn, isDirectoryColumn, sizeColumn);
        pathView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void btnAddFile() {
        System.out.println("Add File");
        if (pathView.getSelectionModel().isEmpty()) {
            pathView.getRoot().getChildren().add(new TreeItem<>(new FileMessage("New File", false, null, (long)(Math.random()*1000))));
        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            pathView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(new FileMessage("New File", false, null, (long)(Math.random()*1000))));
        } else {
            pathView.getSelectionModel().getSelectedItem().getParent().getChildren().add(new TreeItem<>(new FileMessage("New File", false, null, (long)(Math.random()*1000))));
        }
    }

    public void btnAddFolder() {
        System.out.println("Add Folder");
        if (pathView.getSelectionModel().isEmpty()) {
            pathView.getRoot().getChildren().add(new TreeItem<>(new FileMessage("New Folder", true, null, 0)));
        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            pathView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(new FileMessage("New Folder", true, null, 0)));
        } else {
            pathView.getSelectionModel().getSelectedItem().getParent().getChildren().add(new TreeItem<>(new FileMessage("New Folder", true, null, 0)));
        }
    }

    public void btnDownloadFile() {
        System.out.println("Download File");
        System.out.println(pathView.getSelectionModel().getSelectedItem().getValue().getFileRelativePathName());

    }

    public void btnDeleteFile() {
        System.out.println("Delete File");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you agree?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            System.out.println("You clicked OK");
            pathView.getTreeItem(pathView.getSelectionModel().getFocusedIndex()).getParent().getChildren().remove(pathView.getSelectionModel().getSelectedItem());
        } else if (result.get().getText().equals("Cancel")) {
            System.out.println("You clicked Cancel");
        }
    }

    public void btnExit() {
        System.out.println("Exit");
    }

    public void btnRename(ActionEvent actionEvent) {
        System.out.println("Rename");
    }
}
