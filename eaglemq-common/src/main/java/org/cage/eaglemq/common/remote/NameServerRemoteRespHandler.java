package org.cage.eaglemq.common.remote;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.cache.NameServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.HeartBeatDTO;
import org.cage.eaglemq.common.dto.PullBrokerIpRespDTO;
import org.cage.eaglemq.common.dto.ServiceRegistryRespDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;

/**
 * ClassName: NameServerRemoteRespHandler
 * PackageName: org.cage.eaglemq.common.remote
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午10:44
 * @Version: 1.0
 */
@ChannelHandler.Sharable
public class NameServerRemoteRespHandler extends SimpleChannelInboundHandler<TcpMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == code) {
            //msgId
            ServiceRegistryRespDTO serviceRegistryRespDTO = JSON.parseObject(tcpMsg.getBody(), ServiceRegistryRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(serviceRegistryRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode() == code) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = JSON.parseObject(tcpMsg.getBody(), ServiceRegistryRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(serviceRegistryRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (NameServerResponseCode.HEART_BEAT_SUCCESS.getCode() == code) {
            HeartBeatDTO heartBeatDTO = JSON.parseObject(tcpMsg.getBody(), HeartBeatDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(heartBeatDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (NameServerResponseCode.PULL_BROKER_ADDRESS_SUCCESS.getCode() == code) {
            PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(tcpMsg.getBody(), PullBrokerIpRespDTO.class);
            SyncFuture syncFuture = NameServerSyncFutureManager.get(pullBrokerIpRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        }
    }
}
