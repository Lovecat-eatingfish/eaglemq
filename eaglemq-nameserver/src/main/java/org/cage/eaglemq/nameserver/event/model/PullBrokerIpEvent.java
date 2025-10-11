package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import org.cage.eaglemq.common.event.Event;

@Data
public class PullBrokerIpEvent extends Event {

    private String role;

    private String brokerClusterGroup;
}
