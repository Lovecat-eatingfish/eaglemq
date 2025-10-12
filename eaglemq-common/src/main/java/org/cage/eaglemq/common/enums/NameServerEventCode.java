package org.cage.eaglemq.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: NameServerEventCode
 * PackageName: org.cage.eaglemq.common.enums
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:37
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum NameServerEventCode {
    REGISTRY(1, "注册事件"),
    HEART_BEAT(3, "心跳事件"),
    PULL_BROKER_IP_LIST(11, "拉取broker的主节点ip地址"),
    UN_REGISTRY(2, "下线事件"),

    // name server 集群配置信息
    START_REPLICATION(4, "开启复制"),
    MASTER_START_REPLICATION_ACK(5, "master回应slave节点开启同步"),
    SLAVE_HEART_BEAT(7, "从节点心跳数据"),
    MASTER_REPLICATION_MSG(6, "主从同步数据"),
    SLAVE_REPLICATION_ACK_MSG(8, "从节点接收同步数据成功 返回ack"),

    NODE_REPLICATION_MSG(9, "节点复制数据"),
    NODE_REPLICATION_ACK_MSG(10, "链式复制中数据同步成功信号 ack消息"),
    ;

    private final int code;
    private final String desc;
}
