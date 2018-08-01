package com.cloud.storage.server.Handlers;

import com.cloud.storage.common.CmdMessage;
import com.cloud.storage.common.FileMessage;
import com.cloud.storage.common.Ping;
import com.cloud.storage.common.ServerCallbackMessage;
import com.cloud.storage.server.Functions.CmdManager;
import com.cloud.storage.server.Functions.FileManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SortHandler extends ChannelInboundHandlerAdapter {

    //private static Logger log = Logger.getLogger(SortHandler.class.getName());

    private String userName;
    //private boolean blocked = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null)
                return;
            //log.info("New message from " + userName + ": " + msg.getClass().getName());
            if (msg instanceof CmdMessage) {
                CmdManager.getInstance().processCmd(userName, (CmdMessage) msg, ctx);
                //log.info(userName + ": " + ((CmdMessage) msg).getCmd() + " " + ((CmdMessage) msg).getCmdType());
            } else if (msg instanceof FileMessage) {
                //log.info(userName + ": " + ((FileMessage)msg).getFileRelativePathName() + ": " + ((FileMessage)msg).getData().length);
                try {
                    FileManager.getInstance().writeFile(userName, (FileMessage) msg);
                    ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.OK));
                    ctx.flush();
                    CmdManager.getInstance().processCmd(userName, new CmdMessage("", CmdMessage.CmdType.GET_PATHS_LIST), ctx);
                } catch (IOException e) {
                    //log.log(Level.SEVERE, "Exception: ", e);
                    ctx.write(new ServerCallbackMessage(ServerCallbackMessage.Answer.FAIL));
                }
            } else if (msg instanceof Ping) {
                ctx.writeAndFlush(new Ping());
            } else {
                //log.info("Неопознанный тип сообщения");
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
        //log.log(Level.SEVERE, "Exception: ", cause);
        ctx.close();
    }

}
