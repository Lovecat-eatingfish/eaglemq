package org.cage.eaglemq.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class PullBrokerIpRespDTO extends BaseNameServerRemoteDTO {

    // 拉取单个节点
    private List<String> addressList;

    private List<String> masterAddressList;

    private List<String> slaveAddressList;
}
