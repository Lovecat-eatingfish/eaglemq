package org.cage.eaglemq.nameserver.sync;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.cache.NameServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.StartReplicationRespDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.SyncFuture;

/**
 * ClassName: SlaveNameServerRemoteRespHandler
 * PackageName: org.cage.eaglemq.nameserver.sync
 * Description: name server 之间的netty 异步转同步的handler
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午11:39
 * @Version: 1.0
 */
@ChannelHandler.Sharable
public class SlaveNameServerRemoteRespHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public SlaveNameServerRemoteRespHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();

        if (NameServerEventCode.MASTER_START_REPLICATION_ACK.getCode() == code) {
            StartReplicationRespDTO startReplicationRespDTO = JSON.parseObject(body, StartReplicationRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(startReplicationRespDTO.getMessageId());
            syncFuture.setResponse(tcpMsg);
        }else if (NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode() == code) {
            StartReplicationRespDTO startReplicationRespDTO = JSON.parseObject(body, StartReplicationRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(startReplicationRespDTO.getMessageId());
            syncFuture.setResponse(tcpMsg);
        }

    }



}
