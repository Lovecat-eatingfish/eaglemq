package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

/**
 * @author 32782
 */ // name server 主从同步的消息体事件
@EqualsAndHashCode(callSuper = true)
@Data
public class ReplicationMsgEvent extends Event {

    private Integer type;

    private ServiceInstance serviceInstance;
}
