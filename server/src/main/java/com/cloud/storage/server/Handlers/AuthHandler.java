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

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    //private static Logger log = Logger.getLogger(AuthHandler.class.getName());


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg == null)
                return;
            //log.info("New msg: " + msg.getClass().getName());
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
                //log.info("Wrong auth object, return: " + System.currentTimeMillis());
                return;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //log.log(Level.SEVERE, "Exception: " + cause);
        ctx.close();
    }

    @NotNull
    private void failAnswer(ChannelHandlerContext ctx) {
        //log.info("Fail Answer to " + ctx.channel().remoteAddress());
        ctx.writeAndFlush(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
    }

    @NotNull
    private void okAnswer(ChannelHandlerContext ctx) {
        //log.info("Ok Answer to " + ctx.channel().remoteAddress());
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
