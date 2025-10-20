package org.cage.eaglemq.client.consumer;

import org.cage.eaglemq.common.dto.ConsumeMessage;
import org.cage.eaglemq.common.enums.ConsumeResult;

import java.util.List;

public class SingleThreadMessageConsumeListener implements MessageConsumeListener{

    @Override
    public ConsumeResult consume(List<ConsumeMessage> consumeMessages) {
        return null;
    }
}
