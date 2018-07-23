package com.cloud.storage.common;

import java.util.ArrayList;
import java.util.List;

public class FilesMessage extends AbstractMessage {
    private List<FileMessage> list;

    public FilesMessage() {
        this.list = new ArrayList<>();
    }

    public List<FileMessage> getList() {
        return list;
    } // return Stream?

    public void addToList(FileMessage msg) {
        list.add(msg);
    }

    public void addToList(String fileRelativePathName, boolean isDirectory, byte[] data, long size) {
        list.add(new FileMessage(fileRelativePathName, isDirectory, data, size));
    }
}
