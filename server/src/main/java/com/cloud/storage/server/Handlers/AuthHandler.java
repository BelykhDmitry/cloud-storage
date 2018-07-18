package com.cloud.storage.server.Handlers;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.ServerCallbackMessage;
import com.cloud.storage.server.Functions.Authorization;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg == null)
                return;
            System.out.println(msg.getClass()); //TODO: Logging
            if(msg instanceof AuthMessage) {
                if(Authorization.getInstance().authorize((AuthMessage) msg)) {
                    ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.OK));
                    ctx.flush();
                    ctx.pipeline().addLast(new SortHandler());
                    ctx.pipeline().remove(this);
                } else {
                    ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
                    ctx.flush();
                    return;
                }
            } else {
                System.out.println("Wrong auth object, return: " + System.currentTimeMillis());
                return;
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
