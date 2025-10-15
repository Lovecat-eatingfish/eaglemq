package org.cage.eaglemq.broker.slave;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.event.model.CreateTopicEvent;
import org.cage.eaglemq.broker.event.model.PushMsgEvent;
import org.cage.eaglemq.common.cache.BrokerServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.CreateTopicReqDTO;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.dto.SlaveSyncRespDTO;
import org.cage.eaglemq.common.dto.StartSyncRespDTO;
import org.cage.eaglemq.common.enums.BrokerEventCode;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.SyncFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 */
@Slf4j
@ChannelHandler.Sharable
public class SlaveSyncServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private static final Logger logger = LoggerFactory.getLogger(SlaveSyncServerHandler.class);

    private EventBus eventBus;

    public SlaveSyncServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (BrokerEventCode.CREATE_TOPIC.getCode() == code) {
            CreateTopicReqDTO createTopicReqDTO = JSON.parseObject(body,CreateTopicReqDTO.class);
            CreateTopicEvent createTopicEvent= new CreateTopicEvent();
            createTopicEvent.setCreateTopicReqDTO(createTopicReqDTO);
            createTopicEvent.setMsgId(createTopicReqDTO.getMsgId());
            event = createTopicEvent;
        } else if (BrokerEventCode.PUSH_MSG.getCode() == code) {
            MessageDTO messageDTO = JSON.parseObject(body, MessageDTO.class);
            PushMsgEvent pushMsgEvent = new PushMsgEvent();
            pushMsgEvent.setMessageDTO(messageDTO);
            pushMsgEvent.setMsgId(messageDTO.getMessageId());
            logger.info("broker的slave节点收到broker master消息推送同步的内容:{},message is {}", new String(messageDTO.getBody()), JSON.toJSONString(messageDTO));
            event = pushMsgEvent;
        } else if (BrokerResponseCode.START_SYNC_SUCCESS.getCode() == code) {
            StartSyncRespDTO startSyncRespDTO = JSON.parseObject(body, StartSyncRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(startSyncRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        } else if (BrokerResponseCode.SLAVE_SYNC_RESP.getCode() == code) {
            SlaveSyncRespDTO slaveSyncRespDTO = JSON.parseObject(body, SlaveSyncRespDTO.class);
            SyncFuture syncFuture = BrokerServerSyncFutureManager.get(slaveSyncRespDTO.getMsgId());
            if (syncFuture != null) {
                syncFuture.setResponse(tcpMsg);
            }
        }
        if (event != null) {
            event.setChannelHandlerContext(channelHandlerContext);
            eventBus.publish(event);
        }
    }
}
