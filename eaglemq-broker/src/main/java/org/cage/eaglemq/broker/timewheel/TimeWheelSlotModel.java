package org.cage.eaglemq.broker.timewheel;

import lombok.Data;

@Data
public class TimeWheelSlotModel {

    /**
     * 需要延迟的秒数
     */
    private int delaySeconds;

    /**
     * 元数据
     */
    private Object data;

    /**
     * 存储类型
     *
     * @see SlotStoreTypeEnum
     */
    private Class storeType;

    /**
     * 下一次执行时间
     */
    private long nextExecuteTime;
}
