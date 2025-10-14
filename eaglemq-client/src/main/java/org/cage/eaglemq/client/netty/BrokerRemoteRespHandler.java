package org.cage.eaglemq.client.netty;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.cache.BrokerServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.SendMessageToBrokerResponseDTO;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.SyncFuture;

/**
 * ClassName: BrokerRemoteRespHandler
 * PackageName: org.cage.eaglemq.client.netty
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/14 上午1:51
 * @Version: 1.0
 */
@ChannelHandler.Sharable
public class BrokerRemoteRespHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public BrokerRemoteRespHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        if (BrokerResponseCode.SEND_MSG_RESP.getCode() == code) {
            SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = JSON.parseObject(body, SendMessageToBrokerResponseDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(sendMessageToBrokerResponseDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        }
    }
}
