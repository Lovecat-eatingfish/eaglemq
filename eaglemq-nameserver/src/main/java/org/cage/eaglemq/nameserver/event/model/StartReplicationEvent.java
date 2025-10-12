package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import org.cage.eaglemq.common.event.Event;

/**
 * @author 32782
 */
@Data
public class StartReplicationEvent extends Event {

    private String user;
    private String password;
    private String slaveIp;
    private String slavePort;
}
