package org.cage.eaglemq.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsumeMsgBaseRespDTO extends BaseBrokerRemoteDTO {

    private List<ConsumeMsgRespDTO> consumeMsgRespDTOList;
}
