package org.cage.eaglemq.broker;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.config.EagleMqTopicLoader;
import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.config.GlobalPropertiesLoader;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;

import java.io.IOException;

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

    private static void initProperties() throws IOException {
        GlobalPropertiesLoader globalPropertiesLoader = new GlobalPropertiesLoader();
        EagleMqTopicLoader eagleMqTopicLoader = new EagleMqTopicLoader();
        // 加载全局配置文件
        globalPropertiesLoader.loadProperties();
        // 加载broker 的topic四年西
        eagleMqTopicLoader.loadProperties();
        // 开启topic 的定时刷新功能
        eagleMqTopicLoader.startRefreshEagleMqTopicInfoTask();

        // 加载 消费者的偏移量 的相关东西

        // 定时刷新磁盘消费者的偏移量的offset

        // 预备映射 commit log 文件到  内存映射中
        // 预备映射  这个consume queue 的东西
        for (EagleMqTopicModel eagleMqTopicModel : CommonCache.getEagleMqTopicModelList()   ) {

        }
    }

    public static void main(String[] args) throws IOException {
        initProperties();

    }
}
