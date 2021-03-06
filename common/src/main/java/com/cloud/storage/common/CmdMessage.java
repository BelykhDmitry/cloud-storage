package com.cloud.storage.common;

public class CmdMessage extends AbstractMessage {

    // Сообщение с командами пользователя.

    public enum CmdType {
        CREATE_FOLDER,
        REMOVE_FOLDER,
        GET_FILE,
        REMOVE_FILE,
        GET_PATHS_LIST,
        GET_FOLDER,     //Нужно ли. Передавать как очередь сообщений начиная с корневой папки рекурсивно
        CHANGE_PASS,
        RENAME
    }

    private CmdType cmdType;

    private String cmd;

    public CmdMessage(String cmd, CmdType type) {
        this.cmd = cmd;
        this.cmdType = type;
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public String getCmd() {
        return cmd;
    }
}
