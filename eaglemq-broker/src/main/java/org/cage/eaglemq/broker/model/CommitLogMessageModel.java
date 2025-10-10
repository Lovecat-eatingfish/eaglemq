package org.cage.eaglemq.broker.model;

import lombok.Data;

/**
 * @Author idea
 * @Date: Created in 10:24 2024/3/30
 * @Description commitLog真实数据存储对象模型
 */
@Data
public class CommitLogMessageModel {

    /**
     * 真正的消息内容
     */
    private byte[] content;


}
