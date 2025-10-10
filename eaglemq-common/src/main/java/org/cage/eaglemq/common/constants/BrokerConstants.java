package org.cage.eaglemq.common.constants;

/**
 * ClassName: BrokerConstants
 * PackageName: org.cage.eaglemq.common.constants
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午8:57
 * @Version: 1.0
 */
public class BrokerConstants {
    // 全局配置 环境变量name
    public static final String EAGLE_MQ_HOME = "EAGLE_MQ_HOME";

    // broker 配置地址
    public static final String BROKER_PROPERTIES_PATH = "/config/broker.properties";

    // broker topic 的路径
    public static final String BROKER_TOPIC_PATH = "/config/eaglemq-topic.json";


    // topic 定时刷新时间
    public static final Integer DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP = 3;

    // 消费者offset 定时刷新时间
    public static final Integer DEFAULT_REFRESH_CONSUME_QUEUE_OFFSET_TIME_STEP = 1;

}
