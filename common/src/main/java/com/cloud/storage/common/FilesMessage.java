package com.cloud.storage.common;

import java.util.List;

public class FilesMessage extends AbstractMessage {
    private List<FileMessage> list;

    public FilesMessage(List<FileMessage> list) {
        this.list = list;
    }

    public List<FileMessage> getList() {
        return list;
    } // return Stream?

    public void addToList(FileMessage msg) {
        list.add(msg);
    }
}
