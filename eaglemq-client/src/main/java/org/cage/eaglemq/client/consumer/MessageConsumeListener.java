package org.cage.eaglemq.client.consumer;

import org.cage.eaglemq.common.dto.ConsumeMessage;
import org.cage.eaglemq.common.enums.ConsumeResult;

import java.util.List;

public interface MessageConsumeListener {


    /**
     * 默认的消费处理函数
     *
     * @param consumeMessages
     * @return
     */
    ConsumeResult consume(List<ConsumeMessage> consumeMessages) throws InterruptedException;
}
