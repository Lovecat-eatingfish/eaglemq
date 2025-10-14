package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cage.eaglemq.common.event.Event;

/**
 * @author 32782
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PullBrokerIpEvent extends Event {

    private String role;

    private String brokerClusterGroup;
}
