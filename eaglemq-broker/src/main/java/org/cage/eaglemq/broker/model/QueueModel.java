package org.cage.eaglemq.broker.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: QueueModel
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午9:28
 * @Version: 1.0
 */
@Data
public class QueueModel {

    private Integer id;

    private String fileName;

    // 最早的偏移量
    private Integer lastOffset;
    // 该文件的偏移量最大值
    private Integer offsetLimit;
    // 当前偏移量的值
    private AtomicInteger latestOffset;

    // 剩余的容量
    public int remainCapacity() {
        return this.getOffsetLimit() - this.getLatestOffset().get();
    }
}
