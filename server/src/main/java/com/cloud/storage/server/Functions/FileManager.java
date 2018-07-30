package com.cloud.storage.server.Functions;

import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.logging.Logger;

public class FileManager {

    private static volatile FileManager instance;

    private static Logger log = Logger.getLogger(FileManager.class.getName());

    public static FileManager getInstance() {
        FileManager localInstance = instance;
        if (localInstance == null) {
            synchronized (Authorization.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new FileManager();
                }
            }
        }
        return localInstance;
    }

    private final String rootFolder = "C:\\Users\\Dmitrii\\Cloud\\";  //TODO: Server Settings from File

    //public void setPath(String path) {this.rootFolder = path;}

    public void writeFile(String user, FileMessage msg) throws IOException {
        String path = rootFolder + user + "\\" + msg.getFileRelativePathName();
        log.info("Received from " + user + ": " + msg.getFileRelativePathName() + " " + msg.getChecksum() + ":" + msg.checkSum());
        Files.write(Paths.get(path), msg.getData());
    }
    public FileMessage readFile(String user, String fileRelativePath) throws IOException {
        Path p = Paths.get(rootFolder + user+ "\\" + fileRelativePath);
        log.info(user + " read file " + fileRelativePath);
        return new FileMessage(p.getFileName().toString(), false, Files.readAllBytes(p), Files.size(p));
    }

    public void makeDir(String user, String path) throws IOException {
        Files.createDirectories(Paths.get(rootFolder + user + "\\" + path));
        log.info(user + " create folder " + path);
    }

    public void removeDir(String user, String dirPath) throws IOException {
        if (dirPath.equals("") || dirPath.contains(".."))
            throw new IOException("Попытка удалить корневой каталог пользователя " + user);
        log.info(user + " delete folder " + dirPath);
        Path path = Paths.get(rootFolder + user + "\\" + dirPath);
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void removeFile(String user, String filePath) throws IOException {
        if (filePath.equals("") || filePath.contains(".."))
            throw new IOException("Попытка удалить корневой каталог пользователя " + user);
        log.info(user + " delete " + filePath);
        Files.delete(Paths.get(rootFolder + user + "\\" + filePath));
    }

    public void rename(String user, String cmd) throws IOException {
        String[] names = cmd.split("=>", 2);
        log.info(user + " rename " + cmd);
        if (names.length != 2)
            throw new IOException("Неверная команда");
        if(!new File(rootFolder + user + "\\" + names[0]).renameTo(new File(rootFolder + user + "\\" + names[1]))) {
            throw new IOException("Не удалось переименовать файл");
        }
    }

    public String getXMLTree(String user, String filePath) {
        System.err.println(user + " " + filePath);
        Path p = Paths.get(rootFolder + user + "\\" + filePath);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        try {
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    strBuilder.append("<Dir name=\"").append(dir.getFileName().toString()).append("\">");
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    strBuilder.append("<File name=\"").append(file.getFileName().toString()).append("\" size=\"").append(file.toFile().length()).append("\">").append("</File>");
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    strBuilder.append("</Dir>");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println(strBuilder.toString());
        return strBuilder.toString();
    }

    public TreeItem<FileMessage> getUserTree(String user) {
        return getDirTree(new File(rootFolder + user));
    }

    public TreeItem<FileMessage> getDirTree(File directory) {
        TreeItem<FileMessage> root = new TreeItem<>(new FileMessage(directory.getName(),true, null, 0));
        for(File f : directory.listFiles()) {
            System.out.println("Loading " + f.getName());
            if(f.isDirectory()) { //Then we call the function recursively
                root.getChildren().add(getDirTree(f));
            } else {
                root.getChildren().add(new TreeItem<>(new FileMessage(f.getName(),false, null, f.length())));
            }
        }
        return root;
    }
}
