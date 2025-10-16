package org.cage.eaglemq.broker.event.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.event.model.ConsumeMsgAckEvent;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.reblalance.ConsumerInstance;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.ConsumeMsgAckReqDTO;
import org.cage.eaglemq.common.dto.ConsumeMsgAckRespDTO;
import org.cage.eaglemq.common.enums.AckStatus;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.Listener;

import java.util.List;
import java.util.Map;

@Slf4j
public class ConsumeMsgAckListener implements Listener<ConsumeMsgAckEvent> {

    @Override
    public void onReceive(ConsumeMsgAckEvent event) throws Exception {
        ConsumeMsgAckReqDTO consumeMsgAckReqDTO = event.getConsumeMsgAckReqDTO();
        String topic = consumeMsgAckReqDTO.getTopic();
        String consumeGroup = consumeMsgAckReqDTO.getConsumeGroup();
        Integer queueId = consumeMsgAckReqDTO.getQueueId();
        Integer ackCount = consumeMsgAckReqDTO.getAckCount();
        ConsumeMsgAckRespDTO consumeMsgAckRespDTO = new ConsumeMsgAckRespDTO();
        consumeMsgAckRespDTO.setMsgId(event.getMsgId());
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            //topic不存在，ack失败
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        Map<String, List<ConsumerInstance>> consumerInstanceMap = CommonCache.getConsumeHoldMap().get(topic);
        if (consumerInstanceMap == null || consumerInstanceMap.isEmpty()) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        List<ConsumerInstance> consumeGroupInstances = consumerInstanceMap.get(consumeGroup);
        if (CollectionUtils.isEmpty(consumeGroupInstances)) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }
        String currentConsumeReqId = consumeMsgAckReqDTO.getIp() + ":" + consumeMsgAckReqDTO.getPort();
        ConsumerInstance matchInstance = consumeGroupInstances.stream().filter(item -> {
            return item.getConsumerReqId().equals(currentConsumeReqId);
        }).findAny().orElse(null);
        if (matchInstance == null) {
            consumeMsgAckRespDTO.setAckStatus(AckStatus.FAIL.getCode());
            event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                    JSON.toJSONBytes(consumeMsgAckRespDTO)));
            return;
        }

        //数据的ack，到底应该客户端传递offset过来好 还是在服务端计算offset值好？
        for (int i = 0; i < ackCount; i++) {
            CommonCache.getConsumeQueueConsumeHandler().ack(topic, consumeGroup, queueId);
        }
        log.info("broker receive offset value ,topic is {},consumeGroup is {},queueId is {},ackCount is {}",
                topic, consumeGroup, queueId, ackCount);
        consumeMsgAckRespDTO.setAckStatus(AckStatus.SUCCESS.getCode());
        TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.BROKER_UPDATE_CONSUME_OFFSET_RESP.getCode(),
                JSON.toJSONBytes(consumeMsgAckRespDTO));
        event.getChannelHandlerContext().writeAndFlush(tcpMsg);
    }
}
