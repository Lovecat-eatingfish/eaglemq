package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class ConsumeMsgReqDTO extends BaseBrokerRemoteDTO {

    private String topic;
    private String consumeGroup;
    private String ip;
    private Integer port;
    private Integer batchSize;
}
