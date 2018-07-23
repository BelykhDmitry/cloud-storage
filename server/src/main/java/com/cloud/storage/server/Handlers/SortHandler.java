package com.cloud.storage.server.Handlers;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import com.cloud.storage.server.Functions.CmdManager;
import com.cloud.storage.server.Functions.FileManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;

public class SortHandler extends ChannelInboundHandlerAdapter {

    private String userName;
    private boolean blocked = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("SortHandler In Read");
        System.out.flush();
        try {
            if (msg == null)
                return;
            System.out.println(msg.getClass()); //TODO: Logging
            System.out.flush();
            if (msg instanceof CmdMessage) {
                CmdManager.getInstance().processCmd(userName, (CmdMessage) msg, ctx);
                System.out.println(((CmdMessage) msg).getCmd());
                System.out.flush();
                ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
                ctx.flush();
            } else if (msg instanceof FileMessage) {
                System.out.println(((FileMessage)msg).getFileRelativePathName() + ": " + ((FileMessage)msg).getData().length);
                System.out.flush();
                FileManager.getInstance().writeFile(userName, (FileMessage)msg);
                ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.OK));
                ctx.flush();
            } else {
                System.out.println("Неопознанный тип сообщения");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
