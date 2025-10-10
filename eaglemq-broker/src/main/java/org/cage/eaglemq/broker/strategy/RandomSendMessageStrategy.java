package org.cage.eaglemq.broker.strategy;

import org.cage.eaglemq.broker.model.EagleMqTopicModel;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: RandomSendMessageStrategy
 * PackageName: org.cage.eaglemq.broker.strategy
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午10:13
 * @Version: 1.0
 */
public class RandomSendMessageStrategy implements SendMessageStrategy{
    private final Random random = new Random();
    private final int totalQueueId;


    public RandomSendMessageStrategy(EagleMqTopicModel eagleMqTopicModel) {
        this.totalQueueId = eagleMqTopicModel.getQueueList().size();
    }
    @Override
    public int getQueueId() {
        // 返回一个从0到totalQueueId(不包括)的随机整数
        return random.nextInt(totalQueueId);
    }
}
