package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class SlaveSyncRespDTO extends BaseBrokerRemoteDTO {

    private boolean syncSuccess;
}
