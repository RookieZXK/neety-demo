package com.example.nettydemo.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: websocket建立连接
 * @author: zhaoxueke
 * @date 2021/09/10 11:13
 **/
public class WsHandshakeCompleteHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketFrameHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            LOGGER.info("ws连接建立");

            Channel channel = ctx.channel();
            String uuid = channel.attr(TextWebSocketFrameHandler.CHANNEL_UUID_KEY).get().get("uuid");
            // 广播发送至其他人
            ChannelGroup channels = ChannelManager.getChannelGroup();
            for (Channel temp : channels) {
                if (temp != channel) {
                    temp.writeAndFlush(new TextWebSocketFrame("[SERVER]" + "欢迎" + uuid + "加入群聊"));
                }
            }

            ctx.writeAndFlush(new TextWebSocketFrame("[SERVER]" + "hello, " + uuid));
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
