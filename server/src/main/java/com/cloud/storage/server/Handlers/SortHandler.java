package com.cloud.storage.server.Handlers;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class SortHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("SortHandler In Read");
        try {
            if (msg == null)
                return;
            System.out.println(msg.getClass()); //TODO: Logging
            if (msg instanceof CmdMessage) {
                System.out.println(((CmdMessage) msg).getCmd());
                ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
                ctx.flush();
            } else if (msg instanceof FileMessage) {
                //FileMessage m = (FileMessage) msg;
            } else {
                System.out.println("Неопознанный тип сообщения");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
