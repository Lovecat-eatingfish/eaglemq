package org.cage.eaglemq.broker.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.cache.CommonThreadPoolConfig;
import org.cage.eaglemq.broker.model.ConsumeQueueOffsetModel;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.utils.FileContentUtil;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: ConsumeQueueOffsetLoader
 * PackageName: org.cage.eaglemq.broker.config
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午3:41
 * @Version: 1.0
 */
@Slf4j
public class ConsumeQueueOffsetLoader {
    private String filePath;


    public void loadProperties() {
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String basePath = globalProperties.getEagleMqHome();
        if (StringUtil.isNullOrEmpty(basePath)) {
            throw new IllegalArgumentException("EAGLE_MQ_HOME is invalid!");
        }
        filePath = basePath + BrokerConstants.BROKER_CONSUME_OFFSET_PATH;
        String brokerConsumeOffsetInfo = FileContentUtil.readFromFile(filePath);
        log.info("broker 消费者的offset信息：{}", brokerConsumeOffsetInfo);
        ConsumeQueueOffsetModel consumeQueueOffsetModels = JSON.parseObject(brokerConsumeOffsetInfo, ConsumeQueueOffsetModel.class);
        CommonCache.setConsumeQueueOffsetModel(consumeQueueOffsetModels);
    }

    /**
     * 开启一个刷新内存到磁盘的任务
     */
    public void startRefreshConsumeQueueOffsetTask() {
        //异步线程
        //每3秒将内存中的配置刷新到磁盘里面
        CommonThreadPoolConfig.refreshConsumeQueueOffsetExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(BrokerConstants.DEFAULT_REFRESH_CONSUME_QUEUE_OFFSET_TIME_STEP);
                        ConsumeQueueOffsetModel consumeQueueOffsetModel = CommonCache.getConsumeQueueOffsetModel();
                        FileContentUtil.overWriteToFile(filePath, JSON.toJSONString(consumeQueueOffsetModel, SerializerFeature.PrettyFormat));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }

}
