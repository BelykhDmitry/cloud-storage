package com.cloud.storage.client;

import com.sun.istack.internal.NotNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class FileBrowser implements Initializable {

    @FXML
    TreeView<String> pathView;

    @FXML
    VBox mainVBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathView = new TreeView<>();
        TreeItem root = getNodesForDirectory(new File(System.getProperty("user.home")));
        pathView.setRoot(root);
        pathView.refresh();
    }

    @NotNull
    public TreeItem<String> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
        TreeItem<String> root = new TreeItem<>(directory.getName());
        for(File f : directory.listFiles()) {
            System.out.println("Loading " + f.getName());
            if(f.isDirectory()) { //Then we call the function recursively
                root.getChildren().add(new TreeItem<>(f.getName()));
            } else {
                root.getChildren().add(new TreeItem<>(f.getName()));
            }
        }
        return root;
    }

    public void btnDownload() {}

    public void btnCancel() {}
}
