package org.cage.eaglemq.broker.cache;

import io.netty.channel.ChannelHandlerContext;
import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.core.*;
import org.cage.eaglemq.broker.model.*;
import org.cage.eaglemq.broker.netty.nameserver.HeartBeatTaskManager;
import org.cage.eaglemq.broker.netty.nameserver.NameServerClient;
import org.cage.eaglemq.broker.reblalance.ConsumerInstance;
import org.cage.eaglemq.broker.reblalance.ConsumerInstancePool;
import org.cage.eaglemq.broker.timewheel.TimeWheelModelManager;
import org.cage.eaglemq.common.dto.TxMessageAckModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // ack 和 consume 高层面方法
    private static ConsumeQueueConsumeHandler consumeQueueConsumeHandler;

    // broker 连接name server 的客户端
    private static NameServerClient nameServerClient = new NameServerClient();

    // broker 的心跳任务管理者  一个broker 只有一个  一个broker 之和一个， 直接通过static 来控制个数
    private static HeartBeatTaskManager heartBeatTaskManager = new HeartBeatTaskManager();

    // broker 主从同步的 channel集合
    private static Map<String, ChannelHandlerContext> slaveChannelMap = new HashMap<>();

    // topic: 多个消费组（key：消费组name： value： 消费者实例集合）
    private static Map<String,Map<String,List<ConsumerInstance>>> consumeHoldMap = new ConcurrentHashMap<>();

    // 消费者实例池
    private static ConsumerInstancePool consumerInstancePool = new ConsumerInstancePool();

    // 时间轮算法组件
    private static TimeWheelModelManager timeWheelModelManager = new TimeWheelModelManager();

    // 本地事务表
    private static Map<String, TxMessageAckModel> txMessageAckModelMap = new ConcurrentHashMap<>();


    public static Map<String, TxMessageAckModel> getTxMessageAckModelMap() {
        return txMessageAckModelMap;
    }

    public static void setTxMessageAckModelMap(Map<String, TxMessageAckModel> txMessageAckModelMap) {
        CommonCache.txMessageAckModelMap = txMessageAckModelMap;
    }

    public static TimeWheelModelManager getTimeWheelModelManager() {
        return timeWheelModelManager;
    }

    public static void setTimeWheelModelManager(TimeWheelModelManager timeWheelModelManager) {
        CommonCache.timeWheelModelManager = timeWheelModelManager;
    }

    public static ConsumerInstancePool getConsumerInstancePool() {
        return consumerInstancePool;
    }

    public static void setConsumerInstancePool(ConsumerInstancePool consumerInstancePool) {
        CommonCache.consumerInstancePool = consumerInstancePool;
    }

    public static Map<String, Map<String, List<ConsumerInstance>>> getConsumeHoldMap() {
        return consumeHoldMap;
    }

    public static void setConsumeHoldMap(Map<String, Map<String, List<ConsumerInstance>>> consumeHoldMap) {
        CommonCache.consumeHoldMap = consumeHoldMap;
    }

    public static Map<String, ChannelHandlerContext> getSlaveChannelMap() {
        return slaveChannelMap;
    }

    public static void setSlaveChannelMap(Map<String, ChannelHandlerContext> slaveChannelMap) {
        CommonCache.slaveChannelMap = slaveChannelMap;
    }

    public static HeartBeatTaskManager getHeartBeatTaskManager() {
        return heartBeatTaskManager;
    }

    public static void setHeartBeatTaskManager(HeartBeatTaskManager heartBeatTaskManager) {
        CommonCache.heartBeatTaskManager = heartBeatTaskManager;
    }

    public static NameServerClient getNameServerClient() {
        return nameServerClient;
    }

    public static void setNameServerClient(NameServerClient nameServerClient) {
        CommonCache.nameServerClient = nameServerClient;
    }

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
