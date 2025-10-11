package org.cage.eaglemq.broker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * ClassName: QueueAllocationStrategy
 * PackageName: org.cage.eaglemq.broker.enums
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午10:03
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum QueueAllocationStrategyEnum {
    RANDOM("random", "随机"),
    POLLING("polling", "轮询"),
    HASH("hash", "哈希")
    ;
    private final String strategy;
    private  final String desc;


    public static QueueAllocationStrategyEnum find(String strategy) {
        return Arrays.stream(values()).filter(item -> item.strategy.equals(strategy)).findFirst().orElse(null);
    }

}
