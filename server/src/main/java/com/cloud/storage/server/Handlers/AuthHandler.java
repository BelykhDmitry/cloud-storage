package com.cloud.storage.server.Handlers;

import com.cloud.storage.common.AuthMessage;
import com.cloud.storage.common.Ping;
import com.cloud.storage.common.ServerCallbackMessage;
import com.cloud.storage.server.Functions.Authorization;
import com.cloud.storage.server.Functions.FileManager;
import com.sun.istack.internal.NotNull;
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
                AuthMessage auth = (AuthMessage) msg;
                if (auth.isRegistration()) {
                    if(Authorization.getInstance().register(auth)) {
                        FileManager.getInstance().makeDir(auth.getName(),"");
                        authOk(ctx, auth.getName());
                    } else {
                        failAnswer(ctx);
                    }
                } else {
                    if (Authorization.getInstance().authorize(auth)) {
                        authOk(ctx, auth.getName());
                    } else {
                        failAnswer(ctx);
                        return;
                    }
                }
            } else if (msg instanceof Ping) {
                ctx.writeAndFlush(new Ping());
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

    @NotNull
    private void failAnswer(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
    }

    @NotNull
    private void okAnswer(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new ServerCallbackMessage(ServerCallbackMessage.Answer.OK));
    }

    private void authOk(ChannelHandlerContext ctx, String name) {
        okAnswer(ctx);
        SortHandler ch = new SortHandler();
        ctx.pipeline().addLast(ch);
        ch.setUserName(name);
        ctx.flush();
        ctx.pipeline().remove(this);
    }
}
