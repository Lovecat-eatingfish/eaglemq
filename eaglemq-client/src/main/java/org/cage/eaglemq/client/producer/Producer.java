package org.cage.eaglemq.client.producer;

import org.cage.eaglemq.common.dto.MessageDTO;

public interface Producer {

    /**
     * 同步发送
     *
     * @param messageDTO
     * @return
     */
    SendResult send(MessageDTO messageDTO);

    /**
     * 异步发送
     *
     * @param messageDTO
     * @return
     */
    void sendAsync(MessageDTO messageDTO);


    /**
     * 发送事务消息
     *
     * @param messageDTO
     * @return
     */
    SendResult sendTxMessage(MessageDTO messageDTO);
}
