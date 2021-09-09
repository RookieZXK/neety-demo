package com.example.nettydemo.websocket;

import com.example.nettydemo.exception.FormatException;
import com.example.nettydemo.utils.JsonUtil;
import com.example.nettydemo.utils.RequestUriUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 消息处理类
 * @author: zhaoxueke
 * @date 2021/09/02 11:00
 **/

public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextWebSocketFrameHandler.class);

    public static final AttributeKey<Map<String, String>> CHANNEL_UUID_KEY = AttributeKey.valueOf("netty.channel");

    private String wsUriPath;

    public TextWebSocketFrameHandler() {
    }

    public TextWebSocketFrameHandler(String wsUriPath) {
        super();
        this.wsUriPath = wsUriPath;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            LOGGER.info("ws握手包http请求处理");
            fullHttpRequestHandler(ctx, (FullHttpRequest) msg);
        }

        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {

        // 根据请求数据类型进行分发处理
        if (msg instanceof PingWebSocketFrame) {
            pingWebSocketFrameHandler(ctx, (PingWebSocketFrame) msg);
        } else if (msg instanceof TextWebSocketFrame) {
            textWebSocketFrameHandler(ctx, (TextWebSocketFrame) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            closeWebSocketFrameHandler(ctx, (CloseWebSocketFrame) msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.info("channel就绪, ip:{}", channel.remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        String uuid = "";

        Map<String, String> attr = channel.attr(CHANNEL_UUID_KEY).get();
        if (attr != null) {
            uuid = attr.get("uuid");
            ChannelManager.discard(uuid, channel);
        }

        LOGGER.info("channel关闭, ip:{}, uid:{}", channel.remoteAddress(), uuid);
        super.channelInactive(ctx);
    }


    /**
     * 处理连接请求，客户端WebSocket发送握手包时会执行这一次请求
     *
     * @param ctx
     * @param request
     */
    private void fullHttpRequestHandler(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        Map<String, String> headers = RequestUriUtils.getHeader(request.headers());

        Map<String, String> params = RequestUriUtils.getParams(uri);
        LOGGER.info("客户端请求参数:{}, header:{}", JsonUtil.ObjectToJson(params), JsonUtil.ObjectToJson(headers));

        // 判断请求路径是否跟配置中的一致
        if (wsUriPath.equals(RequestUriUtils.getBasePath(uri))) {

            String uuid = params.get("uuid");

            if (StringUtils.isBlank(uuid)) {
                throw new FormatException("uuid为空,不允许建立连接");
            }

            // 储存一些信息
            Attribute<Map<String, String>> attr = ctx.channel().attr(CHANNEL_UUID_KEY);
            Map<String, String> tempMap = new HashMap<>();
            tempMap.put("uuid", uuid);
            attr.setIfAbsent(tempMap);
            ChannelManager.add(uuid, ctx.channel());

            // 因为有可能携带了参数，导致客户端一直无法返回握手包，因此在校验通过后，重置请求路径
            request.setUri(wsUriPath);
        } else {
            ctx.close();
        }
    }

    /**
     * 处理客户端心跳包
     *
     * @param ctx
     * @param frame
     */
    private void pingWebSocketFrameHandler(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
        ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
    }

    /**
     * 创建连接之后，客户端发送的消息都会在这里处理
     *
     * @param ctx
     * @param msg
     */
    private void textWebSocketFrameHandler(ChannelHandlerContext ctx, TextWebSocketFrame msg) {

        Channel channel = ctx.channel();
        String uuid = channel.attr(CHANNEL_UUID_KEY).get().get("uuid");
        String text = msg.text();

        LOGGER.info("ws消息接收, uuid:{}, content:{}", uuid, text);

        WsTextDTO wsTextDTO = JsonUtil.jsonToEntity(text, WsTextDTO.class);
        if (wsTextDTO == null) {
            throw new FormatException("消息格式异常");
        }

        if (StringUtils.isNotBlank(wsTextDTO.getToUuid())) {
            // 发送给指定某人
            Channel toChannel = ChannelManager.getChannelByUuid(uuid);
            if (toChannel == null) {
                LOGGER.info("用户未连接, uuid:{}", uuid);
                channel.writeAndFlush(new TextWebSocketFrame("[SERVER]" + uuid + "当前不在线"));
                return;
            }

            toChannel.writeAndFlush(new TextWebSocketFrame("[" + uuid + "]" + wsTextDTO.getContent()));
        } else {
            // 广播发送至其他人
            ChannelGroup channels = ChannelManager.getChannelGroup();
            for (Channel temp : channels) {
                if (temp != channel) {
                    temp.writeAndFlush(new TextWebSocketFrame("[" + uuid + "]" + wsTextDTO.getContent()));
                }
            }
        }
    }

    /**
     * 客户端发送断开请求处理
     *
     * @param ctx
     * @param frame
     */
    private void closeWebSocketFrameHandler(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        if (cause instanceof FormatException) {
            LOGGER.error("格式异常,关闭当前连接", cause);
            ctx.close();
            return;
        }

        LOGGER.error("未知异常", cause);
    }
}
