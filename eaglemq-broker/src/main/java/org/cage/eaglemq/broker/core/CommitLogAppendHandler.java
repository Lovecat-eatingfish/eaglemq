package org.cage.eaglemq.broker.core;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.io.IOException;

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
        mapFileModel.loadFileInMMap(topicName, 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
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
