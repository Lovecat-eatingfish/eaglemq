package org.cage.eaglemq.broker.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.util.AttributeKey;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.event.model.StartSyncEvent;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.StartSyncRespDTO;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.Listener;

import java.net.InetSocketAddress;

public class StartSyncListener implements Listener<StartSyncEvent> {

    @Override
    public void onReceive(StartSyncEvent event) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) event.getChannelHandlerContext().channel().remoteAddress();
        String reqId = inetSocketAddress.getAddress() + ":" + inetSocketAddress.getPort();
        event.getChannelHandlerContext().attr(AttributeKey.valueOf("reqId")).set(reqId);
        //保存从节点的channel到本地缓存中
        CommonCache.getSlaveChannelMap().put(reqId,event.getChannelHandlerContext());
        StartSyncRespDTO startSyncRespDTO = new StartSyncRespDTO();
        startSyncRespDTO.setMsgId(event.getMsgId());
        startSyncRespDTO.setSuccess(true);
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.START_SYNC_SUCCESS.getCode(), JSON.toJSONBytes(startSyncRespDTO));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }
}
