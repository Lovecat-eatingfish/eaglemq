package org.cage.eaglemq.broker.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.event.Event;

@EqualsAndHashCode(callSuper = true)
@Data
public class PushMsgEvent extends Event {

    private MessageDTO messageDTO;
}
//
