package com.example.nettydemo;

import com.example.nettydemo.websocket.WebSocketChatServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @description: netty启动类
 * @author: zhaoxueke
 * @date 2021/09/02 10:32
 **/

@Service
public class NettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);


    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

    public ChannelFuture start(int port) {
        ChannelFuture channelFuture = null;
        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 1024 * 1024))
                    .childHandler(new WebSocketChatServerInitializer());

            channelFuture = bootstrap.bind(port).sync();

            LOGGER.info("=========netty server start:{}=========", port);
        } catch (Exception e) {
            LOGGER.error("netty start fail", e);
        }
        return channelFuture;
    }

    public void close() {

        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();

        LOGGER.info("=========netty server shutdown=========");
    }

}
