package com.cloud.storage.common;

public class CmdMessage extends AbstractMessage {

    // Сообщение с командами пользователя.
    // TODO: Продумать вариант с Enum

    public enum cmdType {
        CREATE_FOLDER,
        REMOVE_FOLDER,
        GET_FILE,
        GET_FOLDER
    }

    private String cmd;

    public CmdMessage(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }

    // TODO: Add Folders (TreeItem)
    // Functions: 1. File Download? Maybe -> FileMassage
}
