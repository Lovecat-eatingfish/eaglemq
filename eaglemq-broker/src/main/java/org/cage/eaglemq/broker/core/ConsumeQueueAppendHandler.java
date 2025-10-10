package org.cage.eaglemq.broker.core;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.model.QueueModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ConsumeQueueAppendHandler
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:53
 * @Version: 1.0
 */
public class ConsumeQueueAppendHandler {

    /**
     * 预映射 每个主题的 consume queue 文件， 同时放入到manamger 挂历
     * @param topicName
     * @throws IOException
     */
    public void prepareConsumeQueue(String topicName) throws IOException {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        List<QueueModel> queueModelList = eagleMqTopicModel.getQueueList();
        //retry重试topic没有consume queue存在
        if (queueModelList == null) {
            return;
        }
        List<ConsumeQueueMMapFileModel> consumeQueueMMapFileModels = new ArrayList<>();
        //循环遍历，mmap的初始化
        for (QueueModel queueModel : queueModelList) {
            ConsumeQueueMMapFileModel consumeQueueMMapFileModel = new ConsumeQueueMMapFileModel();
            consumeQueueMMapFileModel.loadFileInMMap(
                    topicName,
                    queueModel.getId(),
                    queueModel.getLastOffset(),
                    queueModel.getLatestOffset().get(),
                    queueModel.getOffsetLimit());
            consumeQueueMMapFileModels.add(consumeQueueMMapFileModel);
        }
        CommonCache.getConsumeQueueMMapFileModelManager().put(topicName, consumeQueueMMapFileModels);
    }
}
