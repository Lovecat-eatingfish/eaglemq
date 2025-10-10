package org.cage.eaglemq.broker.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author idea
 * @Date: Created in 08:12 2024/4/16
 * @Description
 */
@Data
public class ConsumeQueueOffsetModel {

    // 所有消费者的 offset 对象: key： topic  value 消费者组
    private OffsetTable offsetTable = new OffsetTable();

    @Data
    public static class OffsetTable {
        private Map<String,ConsumerGroupDetail> topicConsumerGroupDetail = new HashMap<>();
    }

    // 消费者组信息： key： 消费者组的name， value： 一个消费组的多个消费者map对象（key： queueId， value： 消费到那个queueId的文件#queueId的offset）
    @Data
    public static class ConsumerGroupDetail {
        private Map<String,Map<String,String>> consumerGroupDetailMap = new HashMap<>();;

    }
}
