package com.cloud.storage.server.Functions;

import com.cloud.storage.common.FileMessage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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
            String path = rootFolder + user+ "\\" + msg.getFileRelativePathName();
            //Files.createDirectories(Paths.get(path).getRoot());
            Files.write(Paths.get(path), msg.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileMessage readFile(String user, String fileRelativePath) throws IOException {
        return new FileMessage(fileRelativePath, Files.readAllBytes(Paths.get(rootFolder + user+ "\\" + fileRelativePath)));
    }

    public boolean makeDir(String path) {
        boolean result = false;
        try {
            Files.createDirectories(Paths.get(rootFolder + path));
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void removeDir(String user, String dirPath) throws IOException { //TODO: Проверять на пустую строку и на наличие ../
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
        Files.delete(Paths.get(rootFolder + user + "\\" + filePath));
    }
}
