package org.cage.eaglemq.broker.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: EagleMqTopicModel
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午9:26
 * @Version: 1.0
 */
@Data
public class EagleMqTopicModel {

    // topic name
    private String topic;

    // 该topic的 topic 信息
    private CommitLogModel commitLogModel;

    // topic 的queue信息
    private List<QueueModel> queueList;

    private Long createAt;

    private Long updateAt;
}
