package org.cage.eaglemq.broker.netty.broker;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.event.model.*;
import org.cage.eaglemq.common.cache.BrokerServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.*;
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
            ConsumeMsgReqDTO consumeMsgReqDTO = com.alibaba.fastjson2.JSON.parseObject(body, ConsumeMsgReqDTO.class);
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
            consumeMsgReqDTO.setIp(inetSocketAddress.getHostString());
            consumeMsgReqDTO.setPort(inetSocketAddress.getPort());
            ConsumeMsgEvent consumeMsgEvent = new ConsumeMsgEvent();
            consumeMsgEvent.setConsumeMsgReqDTO(consumeMsgReqDTO);
            consumeMsgEvent.setMsgId(consumeMsgReqDTO.getMsgId());
            channelHandlerContext.attr(AttributeKey.valueOf("consumer-reqId")).set(consumeMsgReqDTO.getIp() + ":" + consumeMsgReqDTO.getPort());
            event = consumeMsgEvent;
        } else if (BrokerEventCode.CONSUME_SUCCESS_MSG.getCode() == code) {
            ConsumeMsgAckReqDTO consumeMsgAckReqDTO = com.alibaba.fastjson2.JSON.parseObject(body, ConsumeMsgAckReqDTO.class);
            InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
            consumeMsgAckReqDTO.setIp(inetSocketAddress.getHostString());
            consumeMsgAckReqDTO.setPort(inetSocketAddress.getPort());
            ConsumeMsgAckEvent consumeMsgAckEvent = new ConsumeMsgAckEvent();
            consumeMsgAckEvent.setConsumeMsgAckReqDTO(consumeMsgAckReqDTO);
            consumeMsgAckEvent.setMsgId(consumeMsgAckReqDTO.getMsgId());
            event = consumeMsgAckEvent;
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

    // 如果有消费者 断开连接 需要删除这个消费者实例
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //链接断开的时候，从重平衡池中移除
        Object reqId = ctx.attr(AttributeKey.valueOf("consumer-reqId")).get();
        if (reqId == null) {
            return;
        }
        CommonCache.getConsumerInstancePool().removeFromInstancePool(String.valueOf(reqId));
    }

}
