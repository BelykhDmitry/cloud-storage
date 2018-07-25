package com.cloud.storage.common;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

import java.util.Properties;
import java.util.zip.CRC32;

public class FileMessage extends AbstractMessage {

    // Передача файла от/к Серверу

    private boolean isDirectory; // TODO: Продумать преобразование к универсальному сообщению для передачи файлов/каталогов
    private String fileRelativePathName;
    private long size;
    private byte[] data;
    private long checksum; // Возможно стоит считать хэш сумму для всех полей сообщения и сделать метод абстрактным

    public FileMessage(String fileRelativePathName, boolean isDirectory, byte[] data, long size) {
        this.fileRelativePathName = fileRelativePathName;
        this.isDirectory = isDirectory;
        this.data = data;
        this.size = size;
        checksum = 0;//calcChecksum();
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

    public long getChecksum() {
        return checksum;
    }

    private long calcChecksum() {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        crc32.update(Boolean.toString(isDirectory).getBytes());
        crc32.update(fileRelativePathName.getBytes());
        crc32.update(Double.toString(size).getBytes());
        return crc32.getValue();
    }

    public boolean checkSum() {
        return checksum == calcChecksum();
    }

    //public StringProperty getRelativeNameProperty() {return fileRelativePathName;}
    //public BooleanProperty getIsDirectoryProperty() {return isDirectory;}
    //public StringProperty getSizeProperty() {return new SimpleStringProperty(Long.toString(size.get()));}
}
