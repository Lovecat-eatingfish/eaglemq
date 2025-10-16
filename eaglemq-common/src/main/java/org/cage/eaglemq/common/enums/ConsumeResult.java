package org.cage.eaglemq.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ConsumeResult {

    /**
     * 消费结果
     */
    private int consumeResultStatus;

    public static ConsumeResult CONSUME_SUCCESS() {
        return new ConsumeResult(ConsumeResultStatus.CONSUME_SUCCESS.getCode());
    }

    public static ConsumeResult CONSUME_LATER() {
        return new ConsumeResult(ConsumeResultStatus.CONSUME_LATER.getCode());
    }
}
