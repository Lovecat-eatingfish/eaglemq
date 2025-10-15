package org.cage.eaglemq.broker.netty.broker;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.event.model.CreateTopicEvent;
import org.cage.eaglemq.broker.event.model.PushMsgEvent;
import org.cage.eaglemq.broker.event.model.StartSyncEvent;
import org.cage.eaglemq.common.cache.BrokerServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.CreateTopicReqDTO;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.dto.SlaveSyncRespDTO;
import org.cage.eaglemq.common.dto.StartSyncReqDTO;
import org.cage.eaglemq.common.enums.BrokerEventCode;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.SyncFuture;

import java.net.InetSocketAddress;

/**
 * ClassName: BrokerServerHandler
 * PackageName: org.cage.eaglemq.broker.netty.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午11:00
 * @Version: 1.0
 */
@ChannelHandler.Sharable
@Slf4j
public class BrokerServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public BrokerServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (BrokerEventCode.PUSH_MSG.getCode() == code) {
            MessageDTO messageDTO = JSON.parseObject(body, MessageDTO.class);
            PushMsgEvent pushMsgEvent = new PushMsgEvent();
            pushMsgEvent.setMessageDTO(messageDTO);
            pushMsgEvent.setMsgId(messageDTO.getMessageId());
            log.info("收到消息推送内容:{},message is {}", new String(messageDTO.getBody()), JSON.toJSONString(messageDTO));
            event = pushMsgEvent;
        } else if (BrokerEventCode.CONSUME_MSG.getCode() == code) {

        } else if (BrokerEventCode.CONSUME_SUCCESS_MSG.getCode() == code) {

        } else if (BrokerEventCode.CONSUME_LATER_MSG.getCode() == code) {

        } else if (BrokerEventCode.CREATE_TOPIC.getCode() == code) {
            CreateTopicReqDTO createTopicReqDTO = com.alibaba.fastjson2.JSON.parseObject(body, CreateTopicReqDTO.class);
            CreateTopicEvent createTopicEvent = new CreateTopicEvent();
            createTopicEvent.setCreateTopicReqDTO(createTopicReqDTO);
            createTopicEvent.setMsgId(createTopicReqDTO.getMsgId());
            event = createTopicEvent;
        } else if (BrokerEventCode.START_SYNC_MSG.getCode() == code) {
            StartSyncReqDTO startSyncReqDTO = JSON.parseObject(body, StartSyncReqDTO.class);
            StartSyncEvent startSyncEvent = new StartSyncEvent();
            startSyncEvent.setMsgId(startSyncReqDTO.getMsgId());
            event = startSyncEvent;
        } else if (BrokerResponseCode.SLAVE_SYNC_RESP.getCode() == code) {
            SlaveSyncRespDTO slaveSyncRespDTO = JSON.parseObject(body, SlaveSyncRespDTO.class);
            SyncFuture slaveAckRsp = BrokerServerSyncFutureManager.get(slaveSyncRespDTO.getMsgId());
            slaveAckRsp.setResponse(slaveSyncRespDTO);
        }
        if (event != null) {
            event.setChannelHandlerContext(channelHandlerContext);
            eventBus.publish(event);
        }
    }
}
