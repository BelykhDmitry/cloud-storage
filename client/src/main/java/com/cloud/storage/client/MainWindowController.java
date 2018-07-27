package com.cloud.storage.client;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    VBox mainVBox;

    @FXML
    TreeTableView<FileStats> pathView;

    volatile TreeItem<FileStats> root;

    MessageController msgController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Init");
        initPathView();
        msgController = new MessageController(this);
        Network.getInstance().addListener(msgController);
        Network.getInstance().addToQueue(new CmdMessage("", CmdMessage.CmdType.GET_PATHS_LIST));
    }

    private void initPathView() {
        FileStats msg = new FileStats("/", true, Integer.toString(0));
        root = new TreeItem<>(msg);
        pathView.setRoot(root);
        pathView.setShowRoot(false);
        TreeTableColumn<FileStats, String> nameColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileStats, String> sizeColumn = new TreeTableColumn<>("Size, bytes");
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().getRelativeNameProperty());
        sizeColumn.setCellValueFactory(param -> param.getValue().getValue().getSizeProperty());
        sizeColumn.setComparator((o1, o2) -> (int) (Long.parseLong(o1) - Long.parseLong(o2)));
        nameColumn.setMinWidth(300);
        sizeColumn.setMinWidth(100);
        pathView.getColumns().setAll(nameColumn, sizeColumn);
        pathView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        pathView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                btnDownloadFile();
            }
        });
    }

    public void btnAddFile() {
        FileChooser chooser = new FileChooser();
        try {
            File file = chooser.showOpenDialog(mainVBox.getScene().getWindow());
            System.out.println(file.getName() + " " + file.length());
            String relativeName;
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
    }

    public void btnAddFolder() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.showAndWait();
        String folderName = dialog.getResult();
        if (!folderName.matches("[a-zA-Zа-яА-Я0-9 ]+")) folderName = "New Folder"; // TODO: Заглушка
        System.out.println("Add Folder");
        if (pathView.getSelectionModel().isEmpty()) {
        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            folderName = getItemPath(pathView.getSelectionModel().getSelectedItem()) + "\\" + folderName;
        } else {
            if(pathView.getSelectionModel().getSelectedItem().getParent().getParent() != null) {
                folderName = getItemPath(pathView.getSelectionModel().getSelectedItem().getParent()) + "\\" + folderName;
            }
        }
        System.out.println(folderName);
        Network.getInstance().addToQueue(new CmdMessage(folderName, CmdMessage.CmdType.CREATE_FOLDER));
    }

    public void btnDownloadFile() {
        System.out.println("Download File");
        System.out.println(pathView.getSelectionModel().getSelectedItem().getValue().getRelativeNameProperty().toString());
        String relativeName = null;
        if (pathView.getSelectionModel().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No File selected", ButtonType.OK, ButtonType.CANCEL).showAndWait();
        } else if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
            new Alert(Alert.AlertType.ERROR, "Selected Item is a Directory. Please, select File to Download", ButtonType.OK, ButtonType.CANCEL).showAndWait();
        } else {
            relativeName = getItemPath(pathView.getSelectionModel().getSelectedItem());
            DirectoryChooser chooser = new DirectoryChooser();
            File file = chooser.showDialog(mainVBox.getScene().getWindow());
            if(file == null) {
                return;
            }
            msgController.setFileSavePath(file.getAbsolutePath());
            Network.getInstance().addToQueue(new CmdMessage(relativeName, CmdMessage.CmdType.GET_FILE));
        }
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
        } else if (result.get().getText().equals("Cancel")) {
            System.out.println("You clicked Cancel");
        }
    }

    public void btnExit() {
        System.out.println("Exit");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to Exit?", ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
            System.out.println("You clicked OK");
            ((Stage) mainVBox.getScene().getWindow()).close();
        } else {}
    }

    public void btnRename() {
        System.out.println("Rename");
        if (pathView.getSelectionModel().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No File or Folder selected", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.showAndWait();
        String fileName = dialog.getResult();
        if (fileName == null || !fileName.matches("[a-zA-Zа-яА-Я0-9 ]+")) {
            new Alert(Alert.AlertType.ERROR, "New File Name must contain Chars and Numbers", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            return;
        } else {
            String path = getItemPath(pathView.getSelectionModel().getSelectedItem());
            String path2;
            if (pathView.getSelectionModel().getSelectedItem().getParent().getParent() == null) {
                if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
                    path2 = fileName;
                } else {
                    path2 = fileName + path.substring(path.indexOf("."));
                }
            } else {
                if (pathView.getSelectionModel().getSelectedItem().getValue().isDirectory()) {
                    path2 = getItemPath(pathView.getSelectionModel().getSelectedItem().getParent()) + "\\" + fileName;
                } else {
                    path2 = getItemPath(pathView.getSelectionModel().getSelectedItem().getParent()) + "\\" + fileName + path.substring(path.indexOf("."));
                }
            }
            System.out.println(path + " " + path2);
            Network.getInstance().addToQueue(new CmdMessage(path+"=>"+path2, CmdMessage.CmdType.RENAME));
        }
    }

    public void setTreeRoot(TreeItem<FileStats> root) {
        this.root = root;
        Platform.runLater(this::btnRefresh);
    }

    public void btnRefresh() {this.pathView.setRoot(root); this.pathView.refresh();}

    private String getItemPath(TreeItem<FileStats> item) {
        String path = item.getValue().getRelativeNameProperty().get();
        if(item.getParent() != null && item.getParent().getParent() != null) {
            return getItemPath(item.getParent()) + "\\" + path;
        } else return path;
    }

    public void serverDisconnected() {
        new Alert(Alert.AlertType.ERROR, "Server disconnected :(", ButtonType.OK, ButtonType.CANCEL).showAndWait();
        changeScreen();
    }

    public void btnDisconnect() {
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION, "You want to disconnect from server?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
        if (result.get().getText().equals("OK")) {
            try {
                Network.getInstance().removeListener(msgController);
                Parent root = FXMLLoader.load(getClass().getResource("/auth.fxml"));
                Stage stage = (Stage) mainVBox.getScene().getWindow();
                stage.setScene(new Scene(root, 400, 400));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeScreen() {
        try {
            Network.getInstance().removeListener(msgController);
            Parent root = FXMLLoader.load(getClass().getResource("/auth.fxml"));
            Stage stage = (Stage) mainVBox.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
