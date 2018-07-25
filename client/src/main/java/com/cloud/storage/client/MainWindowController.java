package com.cloud.storage.client;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    VBox mainVBox;

    @FXML
    TreeTableView<FileStats> pathView;

    volatile TreeItem<FileStats> root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Init");
        initPathView();
        MessageController messageController = new MessageController(this);
        Network.getInstance().addListener(messageController); // Как узнать, когда окно закрывается? Чтобы отписаться от рыссылки. Возможно стоит подписываться и отписываться когда сообщение ожидается
        Network.getInstance().addToQueue(new CmdMessage("", CmdMessage.CmdType.GET_PATHS_LIST));
    }

    private void initPathView() {
        //Запрос на сервак
        FileStats msg = new FileStats("/", true, Integer.toString(0));
        root = new TreeItem<>(msg);
        pathView.setRoot(root);
        pathView.setShowRoot(false);
        TreeTableColumn<FileStats, String> nameColumn = new TreeTableColumn<>("Name");
        //TreeTableColumn<FileMessage, Boolean> isDirectoryColumn = new TreeTableColumn<>("Is Directory");
        TreeTableColumn<FileStats, String> sizeColumn = new TreeTableColumn<>("Size");
        nameColumn.setEditable(true);
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getRelativeNameProperty());
        //isDirectoryColumn.setCellValueFactory(param -> param.getValue().getValue().getIsDirectoryProperty());
        sizeColumn.setCellValueFactory(param -> param.getValue().getValue().getSizeProperty());
        pathView.getColumns().setAll(nameColumn, sizeColumn);
        pathView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void btnAddFile() {
        FileChooser chooser = new FileChooser();
        try {
            File file = chooser.showOpenDialog(mainVBox.getScene().getWindow());
            System.out.println(file.getName() + " " + file.length());
            String relativeName = null;
            if (pathView.getSelectionModel().isEmpty()) {
                relativeName = file.getName();
            } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
                relativeName = getItemPath(pathView.getSelectionModel().getSelectedItem()) + "\\" + file.getName();
            } else {
                relativeName = getItemPath(pathView.getSelectionModel().getSelectedItem().getParent()) + "\\" + file.getName();
            }
            try {
                Network.getInstance().addToQueue(new FileMessage(relativeName, file.isDirectory(), Files.readAllBytes(file.toPath()), file.length()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
//        Parent root = null;
//        try {
//            root = FXMLLoader.load(getClass().getResource("/fileBrowser.fxml"));
//            Stage stage = new Stage();
//            stage.setScene(new Scene(root, 600, 400));
//            stage.showAndWait();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        System.out.println("Add File");
//        if (pathView.getSelectionModel().isEmpty()) {
//            pathView.getRoot().getChildren().add(new TreeItem<>(new FileStats("New File", false, Double.toString(Math.random()*1000))));
//        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
//            pathView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(new FileStats("New File", false, Double.toString(Math.random()*1000))));
//        } else {
//            pathView.getSelectionModel().getSelectedItem().getParent().getChildren().add(new TreeItem<>(new FileStats("New File", false, Double.toString(Math.random()*1000))));
//        }
    }

    public void btnAddFolder() {
        System.out.println("Add Folder");
        if (pathView.getSelectionModel().isEmpty()) {
            pathView.getRoot().getChildren().add(new TreeItem<>(new FileStats("New Folder", true, Integer.toString(0))));
        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            pathView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(new FileStats("New Folder", true, Integer.toString(0))));
        } else {
            pathView.getSelectionModel().getSelectedItem().getParent().getChildren().add(new TreeItem<>(new FileStats("New Folder", true, Integer.toString(0))));
        }
    }

    public void btnDownloadFile() {
        System.out.println("Download File");
        System.out.println(pathView.getSelectionModel().getSelectedItem().getValue().getRelativeNameProperty().toString());

    }

    public void btnDeleteFile() {
        System.out.println("Delete File");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you agree?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            System.out.println("You clicked OK");
            String path = getItemPath(pathView.getSelectionModel().getSelectedItem());
            if(pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
                Network.getInstance().addToQueue(new CmdMessage(path, CmdMessage.CmdType.REMOVE_FOLDER));
            } else {
                Network.getInstance().addToQueue(new CmdMessage(path, CmdMessage.CmdType.REMOVE_FILE));
            }
            //pathView.getTreeItem(pathView.getSelectionModel().getFocusedIndex()).getParent().getChildren().remove(pathView.getSelectionModel().getSelectedItem());
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

    public void setTreeRoot(TreeItem<FileStats> root) {
        this.root = root;
    }

    public void btnRefresh() {this.pathView.setRoot(root); this.pathView.refresh();}

    private String getItemPath(TreeItem<FileStats> item) {
        String path = item.getValue().getRelativeNameProperty().get();
        if(item.getParent() != null && item.getParent().getParent() != null) {
            return getItemPath(item.getParent()) + "\\" + path;
        } else return path;
    }
}
