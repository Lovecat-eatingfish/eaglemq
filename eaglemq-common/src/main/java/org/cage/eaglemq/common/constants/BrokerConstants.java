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

    // commit log 文件地址
    public static final String BASE_COMMIT_PATH = "/commitlog/";

    // consumequeue 文件地址
    public static final String BASE_CONSUME_QUEUE_PATH = "/consumequeue/";

    // window文件分隔符
    public static final String SPLIT = "/";


    // broker 配置地址
    public static final String BROKER_PROPERTIES_PATH = "/config/broker.properties";

    // broker topic 的路径
    public static final String BROKER_TOPIC_PATH = "/config/eaglemq-topic.json";

    // broker 消费者偏移量offset 的 文件路径
    public static final String BROKER_CONSUME_OFFSET_PATH = "/config/consumequeue-offset.json";


    // topic 定时刷新时间
    public static final Integer DEFAULT_REFRESH_MQ_TOPIC_TIME_STEP = 3;

    // 消费者offset 定时刷新时间
    public static final Integer DEFAULT_REFRESH_CONSUME_QUEUE_OFFSET_TIME_STEP = 1;

    // commit log 文件大小 ： //1mb单位，方便测试
    public static final Integer COMMIT_LOG_DEFAULT_MMAP_SIZE = 1 * 1024 * 1024;

    // consume queue 文件大小  //1mb单位  方便测试
    public static final Integer COMSUMEQUEUE_DEFAULT_MMAP_SIZE = 1 * 1024 * 1024;


    // 每个consume queue 中消息的大小 = 4 * 4
    //     private int commitLogFileName;
    //
    //    // 消息在 commit log 文件的索引位置
    //    private int messageIndex;
    //
    //    private int messageLength;
    //
    //    private int retryTime;
    public static final int CONSUME_QUEUE_EACH_MSG_SIZE = 16;


    public static final String NAME_SERVER_CONFIG_PATH = "/config/nameserver.properties";

    public static final short DEFAULT_MAGIC_NUM = 17671;
}
