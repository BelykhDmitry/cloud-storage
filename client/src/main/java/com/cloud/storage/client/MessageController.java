package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.application.Platform;
import javafx.scene.control.*;

import javax.xml.parsers.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class MessageController implements InputListener{

    private MainWindowController ctrl;

    private String fileSavePath;

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    MessageController(MainWindowController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public <T extends AbstractMessage> void onMsgReceived(T msg) {
        if (msg instanceof ServerCallbackMessage) {
            System.out.println("New Callback:");
            switch(((ServerCallbackMessage)msg).getStatus()) {
                case DISCONNECTED:
                    Platform.runLater(ctrl::serverDisconnected);
                    break;
                default:
                    System.err.println(((ServerCallbackMessage)msg).getStatus());
            }
        } else if (msg instanceof FileMessage) {
            System.out.println("New File msg:");
            FileMessage in = (FileMessage)msg;
            System.out.println(in.getFileRelativePathName() + " received");
            if(fileSavePath != null) {
                try {
                    System.out.println(fileSavePath+"\\"+in.getFileRelativePathName());
                    Files.write(Paths.get(fileSavePath+"\\"+in.getFileRelativePathName()), in.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    fileSavePath = null;
                }
            }
        } else if (msg instanceof FilesMessage) {
            System.out.println("New Files List:");
            TreeItem<FileStats> root = processXml(((FilesMessage) msg).getXML());
            ctrl.setTreeRoot(root);
        } else {
            System.out.println("Неопознанный тип сообщения");
        }
    }

    private TreeItem<FileStats> processXml(String str) {
        System.out.println(str);
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        TreeItem<FileStats> tree = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
            Node node = doc.getFirstChild();
            tree = getNodes(node);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return tree;
    }

    private TreeItem<FileStats> getNodes(Node node) {
        TreeItem<FileStats> root = new TreeItem<>(new FileStats(node.getAttributes().item(0).getNodeValue(),true,Integer.toString(0)));;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if(child.getNodeName().equals("Dir")) {
                root.getChildren().add(getNodes(child));
            } else {
                root.getChildren().add(new TreeItem<>(new FileStats(child.getAttributes().item(0).getNodeValue(),false,child.getAttributes().item(1).getNodeValue())));
            }
        }
        return root;
    }
}
