package org.cage.eaglemq.nameserver.sync;

import io.netty.channel.Channel;
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
public class SyncSendMessageUtils {

    public static TcpMsg sendSyncMsg(TcpMsg tcpMsg, String messageId) {
        Channel connectNodeChannel = CommonCache.getConnectNodeChannel();
        connectNodeChannel.writeAndFlush(tcpMsg);
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
