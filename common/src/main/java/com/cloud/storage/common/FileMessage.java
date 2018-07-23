package com.cloud.storage.common;

import java.util.List;

public class FileMessage extends AbstractMessage {

    // Передача файла от/к Серверу

    private boolean isDirectory; // TODO: Продумать преобразование к универсальному сообщению для передачи файлов/каталогов
    private String fileRelativePathName;
    private long size;
    private byte[] data;

    public FileMessage(String fileRelativePathName, boolean isDirectory, byte[] data, long size) {
        this.fileRelativePathName = fileRelativePathName;
        this.isDirectory = isDirectory;
        this.data = data;
        this.size = size;
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

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }
}
