package org.cage.eaglemq.broker.timewheel;

import lombok.Data;

@Data
public class DelayMessageDTO {

    /**
     * 原始数据
     */
    private Object data;

    /**
     * @see SlotStoreTypeEnum
     */
    private SlotStoreTypeEnum slotStoreType;

    /**
     * 延迟多久 秒
     */
    private int delay;

    private long nextExecuteTime;
}
