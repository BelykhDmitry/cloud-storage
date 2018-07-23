package com.cloud.storage.server.Functions;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import com.sun.istack.internal.NotNull;
import io.netty.channel.ChannelHandlerContext;

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
    public void processCmd (String user, CmdMessage cmd, ChannelHandlerContext ctx) { // TODO: Реализовать ответы для контекста
        try {
            switch (cmd.getCmdType()) {
                case REMOVE_FOLDER: //CallBack: ServerCallbackMessage OK or FAIL
                    FileManager.getInstance().removeDir(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.FAIL);
                    break;
                case GET_FILE: // TODO: Extra class for multi-file transfer or modification of FileMessage
                    ServerCallBack.fileTransfer(ctx, FileManager.getInstance().readFile(user, cmd.getCmd()));
                    break;
                case CREATE_FOLDER: //CallBack: ServerCallbackMessage OK or FAIL
                    FileManager.getInstance().makeDir(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.FAIL);
                    break;
                case REMOVE_FILE: //CallBack: ServerCallbackMessage OK or FAIL
                    FileManager.getInstance().removeFile(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.FAIL);
                    break;
                case GET_PATHS_LIST:
                    //FileManager.getInstance().getFiles(user, "");
                    ServerCallBack.directoryTransfer(ctx, FileManager.getInstance().getFiles(user, ""));
                    break;
                default:throw new IOException("Неопознанная команда");
            }
        } catch (IOException e) {
            e.printStackTrace();
            ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.FAIL);
        }
    }
}
