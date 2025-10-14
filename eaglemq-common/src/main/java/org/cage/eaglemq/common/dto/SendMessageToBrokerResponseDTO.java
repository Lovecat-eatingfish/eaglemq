package org.cage.eaglemq.common.dto;

import lombok.Data;
import org.cage.eaglemq.common.enums.SendMessageToBrokerResponseStatus;

@Data
public class SendMessageToBrokerResponseDTO extends BaseBrokerRemoteDTO {

    /**
     * 发送消息的结果状态
     *
     * @see SendMessageToBrokerResponseStatus
     */
    private int status;

    private String desc;
}
