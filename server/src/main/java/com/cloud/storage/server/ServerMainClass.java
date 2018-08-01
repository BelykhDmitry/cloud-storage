package com.cloud.storage.server;

import com.cloud.storage.server.Functions.Authorization;
import com.cloud.storage.server.Functions.Service;
import com.cloud.storage.server.Handlers.AuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMainClass {
    private static final int PORT = 8189;
    private static final int MAX_OBJ_SIZE = 1024 * 1024 * 200;

    static final boolean SSL = false;//System.getProperty("ssl") != null;

    //static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8992" : "8023"));

    private static Logger log = Logger.getLogger(ServerMainClass.class.getName());

    public void run() throws Exception {

        final SslContext sslCtx;

        if(SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }
        try {
            Authorization.getInstance().connect();
            EventLoopGroup mainGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(mainGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline p = socketChannel.pipeline();
                                if(sslCtx != null) {
                                    p.addLast(sslCtx.newHandler(socketChannel.alloc()));
                                }
                                p.addLast(
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
        if(args.length == 0)
            new ServerMainClass().run();  // TODO: Сервисный режим. Перенос хранилища, работа с базой данных (установка пароля и тп).
        else {
            Service.runService(args);
        }
    }
}
