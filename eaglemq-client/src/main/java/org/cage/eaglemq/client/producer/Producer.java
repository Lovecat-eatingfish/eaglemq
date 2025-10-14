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
}
