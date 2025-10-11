package org.cage.eaglemq.broker.strategy;

import org.cage.eaglemq.broker.model.EagleMqTopicModel;

/**
 * ClassName: SendMessageStrategy
 * PackageName: org.cage.eaglemq.broker.strategy
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:09
 * @Version: 1.0
 */
public interface SendMessageStrategy {

    int getQueueId();

    // 这个方法是给 hash 策略使用的 ， 因为这个 jdk8 不能有default 的实现，
    int getQueueId(String key);
}
