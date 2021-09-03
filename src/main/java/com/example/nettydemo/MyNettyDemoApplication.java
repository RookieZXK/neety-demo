package com.example.nettydemo;

import io.netty.channel.ChannelFuture;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import javax.annotation.Resource;

@SpringBootApplication
public class MyNettyDemoApplication implements ApplicationRunner {

    @Resource
    private NettyServer nettyServer;

    public static void main(String[] args) {
        SpringApplication.run(MyNettyDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 开启服务
        ChannelFuture channelFuture = nettyServer.start(8888);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> nettyServer.close()));

        if(channelFuture != null){
            channelFuture.channel().closeFuture().sync();
        }
    }
}
