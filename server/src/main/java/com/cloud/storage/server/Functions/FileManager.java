package com.cloud.storage.server.Functions;

public class FileManager {

    private String rootFolder;

    public void setPath(String path) {this.rootFolder = path;} //TODO: Server Settings from File? (Root folder)

    public void writeFile() {}
    public void readFile() {}
    public void makeDir() {}
    public void removeDir() {}
    public void removeFile() {}
}
