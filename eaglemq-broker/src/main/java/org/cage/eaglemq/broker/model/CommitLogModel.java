package org.cage.eaglemq.broker.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: CommitLogModel
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午9:27
 * @Version: 1.0
 */
@Data
public class CommitLogModel {

    // commit log 最新文件的name
    private String fileName;

    // 写入的最大offset
    private Long offsetLimit;

    // 入的最新offset
    private AtomicInteger offset;
}
