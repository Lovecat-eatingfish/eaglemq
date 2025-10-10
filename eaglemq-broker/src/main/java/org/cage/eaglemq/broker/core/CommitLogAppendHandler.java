package org.cage.eaglemq.broker.core;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.enums.QueueAllocationStrategyEnum;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.strategy.PollingSendMessageStrategy;
import org.cage.eaglemq.broker.strategy.RandomSendMessageStrategy;
import org.cage.eaglemq.broker.strategy.SendMessageStrategy;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.io.IOException;
import java.util.Objects;

/**
 * ClassName: CommitLogAppendHandler
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:46
 * @Version: 1.0
 */
@Slf4j
public class CommitLogAppendHandler {

    public void prepareMMapLoading(String topicName) throws IOException {
        CommitLogMMapFileModel mapFileModel = new CommitLogMMapFileModel();
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String brokerQueueAllocationStrategy = globalProperties.getBrokerQueueAllocationStrategy();
        SendMessageStrategy strategy = null;
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        if (Objects.equals(brokerQueueAllocationStrategy, QueueAllocationStrategyEnum.POLLING.getStrategy())) {
            strategy = new PollingSendMessageStrategy(eagleMqTopicModel);
        }else {
            strategy = new RandomSendMessageStrategy(eagleMqTopicModel);
        }
        log.info("queueId 的分配策略是：{}", strategy);
        mapFileModel.loadFileInMMap(topicName, 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE, strategy);
        CommonCache.getCommitLogMMapFileModelManager().put(topicName, mapFileModel);
    }

    // todo 进行broker的主从同步  需要发布事件
    public void appendMessageToCommitLog(MessageDTO messageDTO, int a) throws IOException {


    }

    public void appendMessageToCommitLog(MessageDTO messageDTO) throws IOException {
        CommitLogMMapFileModel mapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(messageDTO.getTopic());
        if (mapFileModel == null) {
            throw new RuntimeException("topic is invalid!");
        }
        mapFileModel.writeContent(messageDTO, true);
    }
}
