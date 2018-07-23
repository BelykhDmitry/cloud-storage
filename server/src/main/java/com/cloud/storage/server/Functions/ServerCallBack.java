package com.cloud.storage.server.Functions;

import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.FilesMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.channel.ChannelHandlerContext;

public class ServerCallBack {
    public static void serverAnswer(ChannelHandlerContext ctx, ServerCallbackMessage.Answer answer) {
        ctx.write(new ServerCallbackMessage(answer));
        ctx.flush();
    }

    public static void fileTransfer(ChannelHandlerContext ctx, FileMessage fileMessage) {
        ctx.write(fileMessage);
        ctx.flush();
    }

    public static void directoryTransfer(ChannelHandlerContext ctx, FilesMessage msg) {
        ctx.write(msg);
        ctx.flush();
    }
}
