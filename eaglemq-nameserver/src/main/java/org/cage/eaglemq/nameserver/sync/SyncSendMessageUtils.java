package org.cage.eaglemq.nameserver.sync;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.cache.NameServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.remote.SyncFuture;
import org.cage.eaglemq.nameserver.common.CommonCache;

import java.util.concurrent.ExecutionException;

/**
 * ClassName: SyncSendMessageUtils
 * PackageName: org.cage.eaglemq.nameserver.sync
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午11:48
 * @Version: 1.0
 */
@Slf4j
public class SyncSendMessageUtils {

    public static TcpMsg sendSyncMsg(TcpMsg tcpMsg, String messageId) {
        Channel connectNodeChannel = CommonCache.getConnectNodeChannel();
        log.info("发送消息前Channel状态 - 是否为null: {}, 是否活跃: {}, 是否可写: {}",
                connectNodeChannel == null,
                connectNodeChannel != null ? connectNodeChannel.isActive() : "N/A",
                connectNodeChannel != null ? connectNodeChannel.isWritable() : "N/A");

        if (connectNodeChannel == null || !connectNodeChannel.isActive()) {
            throw new RuntimeException("与Master节点的连接异常");
        }

        connectNodeChannel.writeAndFlush(tcpMsg);
        log.info("消息已发送到Channel");
        SyncFuture syncFuture = new SyncFuture();
        syncFuture.setMessageId(messageId);
        NameServerSyncFutureManager.put(messageId, syncFuture);
        try {
            return (TcpMsg) syncFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
