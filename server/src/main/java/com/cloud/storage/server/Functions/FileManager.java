package com.cloud.storage.server.Functions;

import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class FileManager {

    private static volatile FileManager instance;

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

    private final String rootFolder = "C:\\Users\\Dmitrii\\Cloud\\";  //TODO: Server Settings from File? (Root folder)

    //public void setPath(String path) {this.rootFolder = path;}

    public void writeFile(String user, FileMessage msg) { // TODO: Продумать, как лучше создавать поддиректории. Склоняюсь к отдельному запросу - не должно сильно загрузить сеть
        try {
            String path = rootFolder + user+ "\\" + msg.getFileRelativePathName(); // FIXME: Проверка поддиректорий
            System.out.println("Received from " + user + ": " + msg.getFileRelativePathName() + " " + msg.getChecksum()+ ":" + msg.checkSum());
            //Files.createDirectories(Paths.get(path).getRoot());
            Files.write(Paths.get(path), msg.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileMessage readFile(String user, String fileRelativePath) throws IOException {
        Path p = Paths.get(rootFolder + user+ "\\" + fileRelativePath);
        return new FileMessage(fileRelativePath, false, Files.readAllBytes(p), Files.size(p));
    }

    public boolean makeDir(String user, String path) {
        boolean result = false;
        try {
            Files.createDirectories(Paths.get(rootFolder + user + "\\" + path));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void removeDir(String user, String dirPath) throws IOException { //TODO: Проверять на пустую строку и на наличие ../
        if (dirPath.equals("") || dirPath.contains(".."))
            throw new IOException("Попытка удалить корневой каталог пользователя " + user);
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
        Files.delete(Paths.get(rootFolder + user + "\\" + filePath));
    }

    public FilesMessage getFiles(String user, String filePath) throws IOException {
        Path p = Paths.get(rootFolder + user + "\\" + filePath);
        FilesMessage msg = new FilesMessage();
        Files.walkFileTree(p, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(dir.compareTo(p) != 0) {
                    msg.addToList(p.relativize(dir).toString(), attrs.isDirectory(), null, attrs.size());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                msg.addToList(p.relativize(file).toString(), attrs.isDirectory(), null, attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return msg;
    }
}
