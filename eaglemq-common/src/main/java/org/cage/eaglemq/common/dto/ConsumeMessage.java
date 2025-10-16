package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class ConsumeMessage {

    private int queueId;

    private ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO;
}
