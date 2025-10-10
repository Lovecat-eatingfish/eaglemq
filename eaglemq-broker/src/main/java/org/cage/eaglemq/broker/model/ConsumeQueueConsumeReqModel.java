package org.cage.eaglemq.broker.model;

import lombok.Data;

/**
 * ClassName: ConsumeQueueConsumeReqModel
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午4:14
 * @Version: 1.0
 */
@Data
public class ConsumeQueueConsumeReqModel {
    //  那个消费者组 消费那个topic 的消息 ， 消费那个queueId 的queue  一次拉取多少条数据
    private String topic;

    private String consumeGroup;

    private int queueId;

    private int batchSize;
}
