package org.cage.eaglemq.broker.cache;

import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.core.*;
import org.cage.eaglemq.broker.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: CommonCache
 * PackageName: org.cage.eaglemq.broker.cache
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午9:10
 * @Version: 1.0
 */
public class CommonCache {
    // broker全局配置属性
    private static GlobalProperties globalProperties = new GlobalProperties();

    // 所哟topic的配置信息
    private static List<EagleMqTopicModel> eagleMqTopicModelList = new ArrayList<>();

    // consume queue 文件映射的管理者
    private static ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager = new ConsumeQueueMMapFileModelManager();

    // commit log 文件映射的管理者
    private static CommitLogMMapFileModelManager commitLogMMapFileModelManager = new CommitLogMMapFileModelManager();

    // 对于consume queue 文件操作 封装的上层对象
    private static ConsumeQueueAppendHandler consumeQueueAppendHandler;

    // 对于  commit log封装的上层对象
    private static CommitLogAppendHandler commitLogAppendHandler;

    //  所有消费者的offset 模型
    private static ConsumeQueueOffsetModel consumeQueueOffsetModel = new ConsumeQueueOffsetModel();

    private static ConsumeQueueConsumeHandler consumeQueueConsumeHandler;

    public static ConsumeQueueConsumeHandler getConsumeQueueConsumeHandler() {
        return consumeQueueConsumeHandler;
    }

    public static void setConsumeQueueConsumeHandler(ConsumeQueueConsumeHandler consumeQueueConsumeHandler) {
        CommonCache.consumeQueueConsumeHandler = consumeQueueConsumeHandler;
    }

    public static ConsumeQueueOffsetModel getConsumeQueueOffsetModel() {
        return consumeQueueOffsetModel;
    }

    public static void setConsumeQueueOffsetModel(ConsumeQueueOffsetModel consumeQueueOffsetModel) {
        CommonCache.consumeQueueOffsetModel = consumeQueueOffsetModel;
    }

    public static ConsumeQueueAppendHandler getConsumeQueueAppendHandler() {
        return consumeQueueAppendHandler;
    }

    public static void setConsumeQueueAppendHandler(ConsumeQueueAppendHandler consumeQueueAppendHandler) {
        CommonCache.consumeQueueAppendHandler = consumeQueueAppendHandler;
    }

    public static CommitLogAppendHandler getCommitLogAppendHandler() {
        return commitLogAppendHandler;
    }

    public static void setCommitLogAppendHandler(CommitLogAppendHandler commitLogAppendHandler) {
        CommonCache.commitLogAppendHandler = commitLogAppendHandler;
    }

    public static CommitLogMMapFileModelManager getCommitLogMMapFileModelManager() {
        return commitLogMMapFileModelManager;
    }

    public static void setCommitLogMMapFileModelManager(CommitLogMMapFileModelManager commitLogMMapFileModelManager) {
        CommonCache.commitLogMMapFileModelManager = commitLogMMapFileModelManager;
    }

    public static ConsumeQueueMMapFileModelManager getConsumeQueueMMapFileModelManager() {
        return consumeQueueMMapFileModelManager;
    }

    public static void setConsumeQueueMMapFileModelManager(ConsumeQueueMMapFileModelManager consumeQueueMMapFileModelManager) {
        CommonCache.consumeQueueMMapFileModelManager = consumeQueueMMapFileModelManager;
    }

    public static GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    public static void setGlobalProperties(GlobalProperties globalProperties) {
        CommonCache.globalProperties = globalProperties;
    }

    public static List<EagleMqTopicModel> getEagleMqTopicModelList() {
        return eagleMqTopicModelList;
    }

    public static void setEagleMqTopicModelList(List<EagleMqTopicModel> eagleMqTopicModelList) {
        CommonCache.eagleMqTopicModelList = eagleMqTopicModelList;
    }

    public static Map<String, EagleMqTopicModel> getEagleMqTopicModelMap() {
        return eagleMqTopicModelList.stream().collect(Collectors.toMap(EagleMqTopicModel::getTopic, item -> item));
    }

}
