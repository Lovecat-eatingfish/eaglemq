package org.cage.eaglemq.broker.cache;

import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.model.QueueModel;

import java.util.ArrayList;
import java.util.List;

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
}
