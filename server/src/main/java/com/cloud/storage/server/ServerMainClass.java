package com.cloud.storage.server;

import com.cloud.storage.server.Functions.Authorization;
import com.cloud.storage.server.Handlers.AuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMainClass {
    private static final int PORT = 8189;
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 200;

    private static Logger log = Logger.getLogger(ServerMainClass.class.getName());

    public void run() throws Exception {
        try {
            Authorization.getInstance().connect();
            EventLoopGroup mainGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(mainGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(MAX_OBJ_SIZE, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new AuthHandler()
                                );
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = b.bind(PORT).sync();
                future.channel().closeFuture().sync();
            } finally {
                mainGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.log(Level.SEVERE, "Exception: ", e);
        } finally {
            Authorization.getInstance().disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        new ServerMainClass().run();  // TODO: Сервисный режим. Перенос хранилища, работа с базой данных (установка пароля и тп).
    }
}
