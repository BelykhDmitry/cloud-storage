package com.cloud.storage.common;

public class CmdMessage extends AbstractMessage {

    // Сообщение с командами пользователя.
    // TODO: Продумать вариант с Enum

    public enum CmdType {
        CREATE_FOLDER,
        REMOVE_FOLDER,
        GET_FILE,
        GET_FOLDER //Продумать реализацию. Через List файлов?
    }

    private CmdType cmdType;

    private String path;

    public CmdMessage(String cmd, CmdType type) {
        this.path = cmd;
        this.cmdType = type;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public String getCmd() {
        return path;
    }

    // TODO: Add Folders (TreeItem)
}
