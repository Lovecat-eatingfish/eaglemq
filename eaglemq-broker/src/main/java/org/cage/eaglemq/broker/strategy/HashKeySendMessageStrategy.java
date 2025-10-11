package org.cage.eaglemq.broker.strategy;

import org.cage.eaglemq.broker.model.EagleMqTopicModel;

/**
 * ClassName: HashKeySendMessageStragey
 * PackageName: org.cage.eaglemq.broker.strategy
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午10:33
 * @Version: 1.0
 */
public class HashKeySendMessageStrategy implements SendMessageStrategy {

    private final int totalQueue;

    public HashKeySendMessageStrategy(EagleMqTopicModel eagleMqTopicModel) {
        totalQueue = eagleMqTopicModel.getQueueList().size();
    }

    @Override
    public int getQueueId() {
        return 0;
    }

    @Override
    public int getQueueId(String key) {
        return key.hashCode() % totalQueue;
    }
}
