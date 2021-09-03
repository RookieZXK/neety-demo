package com.example.nettydemo.websocket;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: zhaoxueke
 * @date 2021/09/02 17:42
 **/
public class ChannelManager {
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);

    private static final ConcurrentHashMap<String, Channel> uuid2ChannelMap = new ConcurrentHashMap<>();

    public static ChannelGroup getChannelGroup() {
        return CHANNEL_GROUP;
    }

    public static void add(String uuid, Channel channel) {
        CHANNEL_GROUP.add(channel);
        uuid2ChannelMap.put(uuid, channel);
    }

    public static boolean discard(String uuid, Channel channel) {
        uuid2ChannelMap.remove(uuid);
        return CHANNEL_GROUP.remove(channel);
    }

    public static Channel getChannelByUuid(String uuid) {
        return uuid2ChannelMap.get(uuid);
    }

}
