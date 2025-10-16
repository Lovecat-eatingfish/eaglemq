package org.cage.eaglemq.client.consumer;

import com.alibaba.fastjson.JSON;
import org.cage.eaglemq.client.netty.BrokerRemoteRespHandler;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.CreateTopicReqDTO;
import org.cage.eaglemq.common.enums.BrokerEventCode;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.BrokerNettyRemoteClient;

import java.util.UUID;

/**
 * @Description
 */
public class CreateTopicClient {


    public void createTopic(String topic, String brokerAddress) {
        String[] brokerAddr = brokerAddress.split(":");
        String ip = brokerAddr[0];
        Integer port = Integer.valueOf(brokerAddr[1]);
        BrokerNettyRemoteClient brokerNettyRemoteClient = new BrokerNettyRemoteClient(ip, port);
        brokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler(new EventBus("mq-client-eventbus")));
        CreateTopicReqDTO createTopicReqDTO = new CreateTopicReqDTO();
        createTopicReqDTO.setTopic(topic);
        createTopicReqDTO.setQueueSize(3);
        TcpMsg respMsg = brokerNettyRemoteClient.sendSyncMsg(new TcpMsg(BrokerEventCode.CREATE_TOPIC.getCode(), JSON.toJSONBytes(createTopicReqDTO)), UUID.randomUUID().toString());
        System.out.println("resp:" + JSON.toJSONString(respMsg));
        brokerNettyRemoteClient.close();
    }
}
