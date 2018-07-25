package com.cloud.storage.server.Functions;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FilesMessage;
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
    public void processCmd (String user, CmdMessage cmd, ChannelHandlerContext ctx) {
        try {
            switch (cmd.getCmdType()) {
                case REMOVE_FOLDER:
                    FileManager.getInstance().removeDir(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.OK);
                    ServerCallBack.directoryTransfer(ctx, new FilesMessage(FileManager.getInstance().getXMLTree(user,"")));
                    break;
                case GET_FILE:
                    ServerCallBack.fileTransfer(ctx, FileManager.getInstance().readFile(user, cmd.getCmd()));
                    break;
                case CREATE_FOLDER:
                    FileManager.getInstance().makeDir(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.OK);
                    ServerCallBack.directoryTransfer(ctx, new FilesMessage(FileManager.getInstance().getXMLTree(user,"")));
                    break;
                case REMOVE_FILE:
                    FileManager.getInstance().removeFile(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.OK);
                    ServerCallBack.directoryTransfer(ctx, new FilesMessage(FileManager.getInstance().getXMLTree(user,"")));
                    break;
                case GET_PATHS_LIST:
                    System.out.println("Before transfer");
                    ServerCallBack.directoryTransfer(ctx, new FilesMessage(FileManager.getInstance().getXMLTree(user,"")));
                    System.out.println("After transfer " + ctx.channel().bytesBeforeWritable());
                    break;
                case CHANGE_PASS:
                    break;
                case RENAME:
                    FileManager.getInstance().rename(user, cmd.getCmd());
                    ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.OK);
                    ServerCallBack.directoryTransfer(ctx, new FilesMessage(FileManager.getInstance().getXMLTree(user,"")));
                    break;
                default:throw new IOException("Неопознанная команда");
            }
        } catch (IOException e) {
            e.printStackTrace();
            ServerCallBack.serverAnswer(ctx, ServerCallbackMessage.Answer.FAIL);
        }
    }
}
