package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class StartSyncRespDTO extends BaseBrokerRemoteDTO {

    private boolean isSuccess;

}
