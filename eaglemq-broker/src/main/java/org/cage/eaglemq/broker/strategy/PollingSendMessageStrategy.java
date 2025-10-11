package org.cage.eaglemq.broker.strategy;

import org.cage.eaglemq.broker.model.EagleMqTopicModel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: PollotSendMessageStrategy
 * PackageName: org.cage.eaglemq.broker.strategy
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:10
 * @Version: 1.0
 */
public class PollingSendMessageStrategy implements SendMessageStrategy {

    private AtomicInteger count;

    private EagleMqTopicModel eagleMqTopicModel;

    public PollingSendMessageStrategy(EagleMqTopicModel eagleMqTopicModel) {
        this.eagleMqTopicModel = eagleMqTopicModel;
        this.count = new AtomicInteger(eagleMqTopicModel.getQueueList().size());
    }

    @Override
    public int getQueueId() {
        return this.count.getAndIncrement() % eagleMqTopicModel.getQueueList().size();
    }

    @Override
    public int getQueueId(String key) {
        return 0;
    }
}
