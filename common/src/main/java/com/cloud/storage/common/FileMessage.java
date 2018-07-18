package com.cloud.storage.common;

public class FileMessage extends AbstractMessage {

    // Передача файла от/к Серверу
    // Оставить как контейнер?

    private String fileRelativePathName;
    private byte[] data;

    public FileMessage(String fileRelativePathName) {
        this.fileRelativePathName = fileRelativePathName;
        this.data = data;
    }

    public String getFileRelativePathName() {
        return fileRelativePathName;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
