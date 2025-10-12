package org.cage.eaglemq.nameserver.store;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: ReplicationChannelManager
 * PackageName: org.cage.eaglemq.nameserver.store
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午12:05
 * @Version: 1.0
 */
public class ReplicationChannelManager {
    // key: slave:ip:port
    private static Map<String, ChannelHandlerContext> channelHandlerContextMap = new ConcurrentHashMap<>();


    public static Map<String, ChannelHandlerContext> getChannelHandlerContextMap() {
        return channelHandlerContextMap;
    }

    public static void setChannelHandlerContextMap(Map<String, ChannelHandlerContext> channelHandlerContextMap) {
        ReplicationChannelManager.channelHandlerContextMap = channelHandlerContextMap;
    }

    public void put(String reqId, ChannelHandlerContext channelHandlerContext) {
        channelHandlerContextMap.put(reqId, channelHandlerContext);
    }

    public void get(String reqId) {
        channelHandlerContextMap.get(reqId);
    }


}
