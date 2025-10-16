package org.cage.eaglemq.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsumeMsgRespDTO {

    /**
     * 队列id
     */
    private Integer queueId;
    /**
     * 拉数据返回内容
     */
    private List<ConsumeMsgCommitLogDTO> commitLogContentList;
}
