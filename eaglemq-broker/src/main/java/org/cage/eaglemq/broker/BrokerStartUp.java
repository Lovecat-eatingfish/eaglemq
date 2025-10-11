package org.cage.eaglemq.broker;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.config.ConsumeQueueOffsetLoader;
import org.cage.eaglemq.broker.config.EagleMqTopicLoader;
import org.cage.eaglemq.broker.config.GlobalPropertiesLoader;
import org.cage.eaglemq.broker.core.CommitLogAppendHandler;
import org.cage.eaglemq.broker.core.ConsumeQueueAppendHandler;
import org.cage.eaglemq.broker.core.ConsumeQueueConsumeHandler;
import org.cage.eaglemq.broker.model.ConsumeQueueConsumeReqModel;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.common.dto.ConsumeMsgCommitLogDTO;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: BrokerStartUp
 * PackageName: org.cage.eaglemq.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午8:54
 * @Version: 1.0
 */
public class BrokerStartUp {

    private static GlobalPropertiesLoader globalPropertiesLoader;

    private static EagleMqTopicLoader eagleMqTopicLoader;

    private static ConsumeQueueOffsetLoader consumeQueueOffsetLoader;

    private static CommitLogAppendHandler commitLogAppendHandler;

    private static ConsumeQueueAppendHandler consumeQueueAppendHandler;


    private static void initProperties() throws IOException {
        // broker 全局配置的加载瘟胶囊
        globalPropertiesLoader = new GlobalPropertiesLoader();
        // broker topic 和 queueModel 加载的loader
        eagleMqTopicLoader = new EagleMqTopicLoader();
        // broker 的所有消费者的offset 加载的loader
        consumeQueueOffsetLoader = new ConsumeQueueOffsetLoader();


        commitLogAppendHandler = new CommitLogAppendHandler();
        consumeQueueAppendHandler = new ConsumeQueueAppendHandler();

        // 加载全局配置文件
        globalPropertiesLoader.loadProperties();

        // 加载broker 的topic四年西
        eagleMqTopicLoader.loadProperties();
        // 开启topic 的定时刷新功能
        eagleMqTopicLoader.startRefreshEagleMqTopicInfoTask();

        // 加载 消费者的偏移量 的相关东西
        consumeQueueOffsetLoader.loadProperties();
        // 定时刷新磁盘消费者的偏移量的offset
        consumeQueueOffsetLoader.startRefreshConsumeQueueOffsetTask();

        // 预备映射 commit log 文件到  内存映射中
        // 预备映射  这个consume queue 的东西
        for (EagleMqTopicModel eagleMqTopicModel : CommonCache.getEagleMqTopicModelList()) {
            String topicName = eagleMqTopicModel.getTopic();
            commitLogAppendHandler.prepareMMapLoading(topicName);
            consumeQueueAppendHandler.prepareConsumeQueue(topicName);
        }

        CommonCache.setCommitLogAppendHandler(commitLogAppendHandler);
        CommonCache.setConsumeQueueAppendHandler(consumeQueueAppendHandler);
        ConsumeQueueConsumeHandler consumeQueueConsumeHandler = new ConsumeQueueConsumeHandler();
        CommonCache.setConsumeQueueConsumeHandler(consumeQueueConsumeHandler);
    }

    private static void initNameServerChannel() throws InterruptedException, UnknownHostException {
        // 初始化连接 这个name server
        CommonCache.getNameServerClient().initConnection();
        // broker 给 这个name serve人发送注册事件
        CommonCache.getNameServerClient().sendRegistryMsgToNameServer();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        initProperties();

        initNameServerChannel();

    }


//    public void testConsumeMessage() {
//        String topic = "created_order_topic";
//        String consumeGroup = "test-consume-group";
//        int queueId = 0;
//        ConsumeQueueConsumeHandler consumeQueueConsumeHandler = CommonCache.getConsumeQueueConsumeHandler();
//        ConsumeQueueConsumeReqModel consumeQueueConsumeReqModel = new ConsumeQueueConsumeReqModel();
//        consumeQueueConsumeReqModel.setTopic(topic);
//        consumeQueueConsumeReqModel.setQueueId(queueId);
//        consumeQueueConsumeReqModel.setConsumeGroup(consumeGroup);
//        consumeQueueConsumeReqModel.setBatchSize(30);
//        List<ConsumeMsgCommitLogDTO> result = consumeQueueConsumeHandler.consume(consumeQueueConsumeReqModel);
//        for (ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO : result) {
//            System.out.println("consumeMsgCommitLogDTO==> " + consumeMsgCommitLogDTO);
//            consumeQueueConsumeHandler.ack(topic, consumeGroup, queueId);
//        }
//        System.out.println("总共的数量： " + result.size());
//    }
}
