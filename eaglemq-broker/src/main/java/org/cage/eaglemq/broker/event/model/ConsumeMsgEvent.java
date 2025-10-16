package org.cage.eaglemq.broker.event.model;

import lombok.Data;
import org.cage.eaglemq.common.dto.ConsumeMsgReqDTO;
import org.cage.eaglemq.common.event.Event;

@Data
public class ConsumeMsgEvent extends Event {

    private ConsumeMsgReqDTO consumeMsgReqDTO;
}
