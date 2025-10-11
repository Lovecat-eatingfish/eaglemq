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

    ;

    private final int code;
    private final String desc;
}
