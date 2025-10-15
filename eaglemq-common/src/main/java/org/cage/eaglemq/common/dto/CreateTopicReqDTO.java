package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class CreateTopicReqDTO extends BaseBrokerRemoteDTO {

    private String topic;

    private Integer queueSize;
}
