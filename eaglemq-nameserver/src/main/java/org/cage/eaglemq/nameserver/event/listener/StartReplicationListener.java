package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.StartReplicationRespDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.common.replication.event.StartReplicationRespEvent;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.StartReplicationEvent;
import org.cage.eaglemq.nameserver.utils.NameserverUtils;

import java.net.InetSocketAddress;

/**
 * ClassName: StartReplicationListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午11:58
 * @Version: 1.0
 */
public class StartReplicationListener implements Listener<StartReplicationEvent> {
    @Override
    public void onReceive(StartReplicationEvent event) throws Exception {
        String msgId = event.getMsgId();
        boolean isVerify = NameserverUtils.isVerify(event.getUser(), event.getPassword());
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();

        StartReplicationRespDTO startReplicationRespDTO = new StartReplicationRespDTO();
        startReplicationRespDTO.setMessageId(msgId);
        if (!isVerify) {
            startReplicationRespDTO.setSuccess(false);
            startReplicationRespDTO.setDesc(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc());
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    JSON.toJSONBytes(startReplicationRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        event.setSlaveIp(inetSocketAddress.getHostString());
        event.setSlavePort(String.valueOf(inetSocketAddress.getPort()));
        String reqId = event.getSlaveIp() + ":" + event.getSlavePort();
        channelHandlerContext.attr(AttributeKey.valueOf("reqId")).set(reqId);
        CommonCache.getReplicationChannelManager().put(reqId, channelHandlerContext);

        startReplicationRespDTO.setSuccess(true);
        startReplicationRespDTO.setDesc("connect success, can start replication master data");
        TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.MASTER_START_REPLICATION_ACK.getCode(), JSON.toJSONBytes(startReplicationRespDTO));
        channelHandlerContext.writeAndFlush(tcpMsg);

    }
}
