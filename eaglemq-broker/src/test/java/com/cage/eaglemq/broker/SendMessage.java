package com.cage.eaglemq.broker;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.core.CommitLogAppendHandler;
import org.cage.eaglemq.broker.core.ConsumeQueueAppendHandler;

import java.io.IOException;

/**
 * ClassName: SendMessage
 * PackageName: com.cage.eaglemq.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午3:55
 * @Version: 1.0
 */
public class SendMessage {
    public static void main(String[] args) throws IOException {
        CommitLogAppendHandler commitLogAppendHandler = CommonCache.getCommitLogAppendHandler();

        ConsumeQueueAppendHandler consumeQueueAppendHandler = CommonCache.getConsumeQueueAppendHandler();
        String topic = "created_order_topic";
//        for (int i = 0; i < 10; i++) {
//            MessageDTO messageDTO = new MessageDTO();
//            messageDTO.setProducerId("producerA");
//            messageDTO.setTopic(topic);
//            messageDTO.setQueueId(0);
//            messageDTO.setBody(("你好" + i).getBytes());
//            messageDTO.setMessageId(UUID.randomUUID().toString());
//            commitLogAppendHandler.appendMessageToCommitLog(messageDTO);
//        }


    }
}
