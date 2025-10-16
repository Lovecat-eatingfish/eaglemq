package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class ConsumeMsgAckRespDTO extends BaseBrokerRemoteDTO {

    /**
     * ack是否成功
     *
     * @see org.cage.eaglemq.common.enums.AckStatus
     */
    private int ackStatus;
}
