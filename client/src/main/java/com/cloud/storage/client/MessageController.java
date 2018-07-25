package com.cloud.storage.client;

import com.cloud.storage.common.AbstractMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import javafx.scene.control.TreeItem;

import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class MessageController implements InputListener{

    private MainWindowController ctrl;

    public MessageController(MainWindowController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public <T extends AbstractMessage> void onMsgReceived(T msg) {
        // Methods from Controller

        if (msg instanceof ServerCallbackMessage) {
            System.out.println("New Callback:");
            System.out.println(((ServerCallbackMessage)msg).getStatus());
        } else if (msg instanceof FileMessage) {
            System.out.println("New File msg:");
            System.out.println(((FileMessage)msg).getFileRelativePathName() + " received");
        } else if (msg instanceof FilesMessage) {
            System.out.println("New Files List:");
            TreeItem<FileStats> root = processXml(((FilesMessage) msg).getXML());
            ctrl.setTreeRoot(root);
        } else {
            System.out.println("Неопознанный тип сообщения");
        }
    }

    private TreeItem<FileStats> processXml(String str) {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        TreeItem<FileStats> tree = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
            //Element element = doc.getDocumentElement();
            Node node = doc.getFirstChild();
            tree = getNodes(node);
//            if(node.hasAttributes()) {
//                for (int i = 0; i < node.getAttributes().getLength(); i++) {
//                    System.out.println(node.getAttributes().item(i).getNodeValue() + " " + node.getAttributes().item(i).getNodeName());
//                }
//                if(node.hasChildNodes()) {
//                    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
//                        Node child = node.getChildNodes().item(i);
//                        System.out.println(child.getNodeName()+ " " + child.getNodeValue() + ":");
//                        if(child.hasAttributes()) {
//                            for (int j = 0; j < child.getAttributes().getLength(); j++) {
//                                System.out.println(i + ": " + child.getAttributes().item(j).getNodeValue() + " " + child.getAttributes().item(j).getNodeName());
//                            }
//                        }
//                    }
//
//                }
//                //tree = new TreeItem<>(new FileStats(node.getAttribute("name"), true, element.getAttribute("size")));
//                //System.out.println("From parser: " + tree.getValue().getRelativeNameProperty().toString());
//
////                for (int i = 0; i < element.getChildNodes().getLength(); i++) {
////                    System.out.println(element.getChildNodes().item(i).getAttributes().getNamedItem("name").getNodeValue());
////                }
//            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public TreeItem<String> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
        TreeItem<String> root = new TreeItem<>(directory.getName());
        for(File f : directory.listFiles()) {
            System.out.println("Loading " + f.getName());
            if(f.isDirectory()) { //Then we call the function recursively
                root.getChildren().add(getNodesForDirectory(f));
            } else {
                root.getChildren().add(new TreeItem<>(f.getName()));
            }
        }
        return root;
    }
}
