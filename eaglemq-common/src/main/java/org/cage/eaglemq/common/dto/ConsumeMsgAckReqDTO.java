package org.cage.eaglemq.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 32782
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConsumeMsgAckReqDTO extends BaseBrokerRemoteDTO {

    private String topic;
    private String consumeGroup;
    private Integer queueId;
    private Integer ackCount;
    private String ip;
    private Integer port;
}
