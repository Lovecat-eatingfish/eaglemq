package org.cage.eaglemq.broker.event.model;

import lombok.Data;
import org.cage.eaglemq.common.dto.ConsumeMsgAckReqDTO;
import org.cage.eaglemq.common.event.Event;

@Data
public class ConsumeMsgAckEvent extends Event {

    private ConsumeMsgAckReqDTO consumeMsgAckReqDTO;
}
