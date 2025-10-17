package org.cage.eaglemq.common.dto;

import lombok.Data;

/**
 * ClassName: MessageDTO
 * PackageName: org.cage.eaglemq.common.dto
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午1:45
 * @Version: 1.0
 */
@Data
public class MessageDTO {
    // 那个生产者发送的
    private String producerId;

    /**
     * @see org.cage.eaglemq.common.enums.MessageSendWay
     * 发送方式
     */
    private int sendWay;


    // 目标主题name
    private String topic;

    // 发送的目标 queueId
    private int queueId = 0;

    // 消息id 可以去重、消息的唯一表示
    private String messageId;


    // 重试属性
    private boolean isRetry;


    private int currentRetryTimes;

    // 延迟属性
    private int delay = -1;

    // 事务消息的类型
    /**
     * @see org.cage.eaglemq.common.enums.TxMessageFlagEnum
     */
    private int txFlag = -1;

    // 本地消息执行状态
    /**
     * @see org.cage.eaglemq.common.enums.LocalTransactionState
     */
    private int localTxState = -1;

    // 消息体
    private byte[] body;
}
