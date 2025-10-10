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
}
