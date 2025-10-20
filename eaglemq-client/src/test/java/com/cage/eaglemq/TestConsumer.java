package com.cage.eaglemq;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.client.consumer.DefaultMqConsumer;
import org.cage.eaglemq.client.consumer.MessageConsumeListener;
import org.cage.eaglemq.common.dto.ConsumeMessage;
import org.cage.eaglemq.common.enums.ConsumeResult;

import java.util.List;

/**
 * ClassName: TestConsumer
 * PackageName: com.cage.eaglemq
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/20 下午11:23
 * @Version: 1.0
 */

@Slf4j
public class TestConsumer {

    public static void main(String[] args) throws InterruptedException {
        testConsumeUserEnterTopicMsg();
    }

    public static void testConsumeUserEnterTopicMsg() throws InterruptedException {
        DefaultMqConsumer consumer = new DefaultMqConsumer();
        consumer.setNsIp("127.0.0.1");
        consumer.setNsPort(9093);
        consumer.setNsPwd("eagle_mq");
        consumer.setNsUser("eagle_mq");
        consumer.setTopic("user_enter");
        consumer.setConsumeGroup("test-consume-user-enter-group");
        consumer.setBrokerClusterGroup("eagle_mq_test_group");
        consumer.setBatchSize(1);
        consumer.setMessageConsumeListener(new MessageConsumeListener() {
            @Override
            public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
                for (ConsumeMessage consumeMessage : consumeMessages) {
                    log.info("retryTimes：{},消费端获取的数据内容:{}", consumeMessage.getConsumeMsgCommitLogDTO().getRetryTimes(),
                            new String(consumeMessage.getConsumeMsgCommitLogDTO().getBody()));
                }
                return ConsumeResult.CONSUME_SUCCESS();
            }
        });
        consumer.start();
    }


    public void testConsumeDelayMsgTopicMsg() throws InterruptedException {
        DefaultMqConsumer consumer = new DefaultMqConsumer();
        consumer.setNsIp("127.0.0.1");
        consumer.setNsPort(9093);
        consumer.setNsPwd("eagle_mq");
        consumer.setNsUser("eagle_mq");
        consumer.setTopic("delay_queue");
        consumer.setConsumeGroup("test-consume-user-enter-group");
        consumer.setBrokerClusterGroup("eagle_mq_test_group");
        consumer.setBatchSize(1);
        consumer.setMessageConsumeListener(new MessageConsumeListener() {
            @Override
            public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
                for (ConsumeMessage consumeMessage : consumeMessages) {
                    log.info("retryTimes：{},延迟消息内容订阅:{}", consumeMessage.getConsumeMsgCommitLogDTO().getRetryTimes(),
                            new String(consumeMessage.getConsumeMsgCommitLogDTO().getBody()));
                }
                return ConsumeResult.CONSUME_SUCCESS();
            }
        });
        consumer.start();
    }
}
