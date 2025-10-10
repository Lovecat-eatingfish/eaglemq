package org.cage.eaglemq.broker.config;

import com.alibaba.fastjson.JSON;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.cache.CommonThreadPoolConfig;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.utils.FileContentUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: EagleMqTopicLoader
 * PackageName: org.cage.eaglemq.broker.config
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午9:14
 * @Version: 1.0
 */
@Slf4j
public class EagleMqTopicLoader {

    private String eagleMqTopicPath = "";

    public void loadProperties() {
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String basePath = globalProperties.getEagleMqHome();
        if (StringUtil.isNullOrEmpty(basePath)) {
            throw new IllegalArgumentException("EAGLE_MQ_HOME is invalid!");
        }
        eagleMqTopicPath = basePath + BrokerConstants.BROKER_TOPIC_PATH;
        String brokerTopicInfo = FileContentUtil.readFromFile(eagleMqTopicPath);
        log.info("broker topic properties :{}", brokerTopicInfo);
        if (StringUtil.isNullOrEmpty(brokerTopicInfo)) {
            throw new IllegalArgumentException("brokerTopic is invalid!");
        }
        List<EagleMqTopicModel> eagleMqTopicModelList = JSON.parseArray(brokerTopicInfo, EagleMqTopicModel.class);
        log.info("eagleMqTopicModel: {}", eagleMqTopicModelList);

        CommonCache.setEagleMqTopicModelList(eagleMqTopicModelList);
    }

    public void startRefreshEagleMqTopicInfoTask() {
        CommonThreadPoolConfig.refreshEagleMqTopicExecutor.execute(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(BrokerConstants.DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP);
                    log.info("【startRefreshEagleMqTopicInfoTask】 =》开始同步topic的数据到磁盘中");
                    List<EagleMqTopicModel> eagleMqTopicModelList = CommonCache.getEagleMqTopicModelList();
                    FileContentUtil.overWriteToFile(eagleMqTopicPath, JSON.toJSONString(eagleMqTopicModelList));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
