package com.cloud.storage.server.Functions;

import com.cloud.storage.common.CmdMessage;
import com.sun.istack.internal.NotNull;

import java.io.IOException;

public class CmdManager {

    private static volatile CmdManager instance;

    public static CmdManager getInstance() {
        CmdManager localInstance = instance;
        if (localInstance == null) {
            synchronized (Authorization.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CmdManager();
                }
            }
        }
        return localInstance;
    }

    @NotNull
    public void processCmd (String user, CmdMessage cmd) {
        try {
            switch (cmd.getCmdType()) {
                case REMOVE_FOLDER:
                    FileManager.getInstance().removeDir(user, cmd.getCmd());
                    break;
                case GET_FILE:
                    FileManager.getInstance().readFile(user, cmd.getCmd()); // TODO: Куда прокинуть файл месседж?
                    break;
                case CREATE_FOLDER:
                    FileManager.getInstance().makeDir(user, cmd.getCmd());
                    break;
                default:throw new IOException("Неопознанная команда");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
