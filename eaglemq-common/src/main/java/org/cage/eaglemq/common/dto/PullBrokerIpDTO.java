package org.cage.eaglemq.common.dto;

import lombok.Data;

@Data
public class PullBrokerIpDTO extends BaseNameServerRemoteDTO {

    private String role;

    private String brokerClusterGroup;
}
