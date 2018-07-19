package com.cloud.storage.server.Functions;

import com.cloud.storage.common.FileMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    private final String rootFolder = "C:\\Users\\Dmitrii\\Cloud\\";

    //public void setPath(String path) {this.rootFolder = path;} //TODO: Server Settings from File? (Root folder)

    public void writeFile(String user, FileMessage msg) {
        try {
            String path = rootFolder + user+ "\\" + msg.getFileRelativePathName();
            //Files.createDirectories(Paths.get(path).getRoot());
            Files.write(Paths.get(path), msg.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readFile(String filePath) {}

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

    public void removeDir() {}
    public void removeFile() {}
}
