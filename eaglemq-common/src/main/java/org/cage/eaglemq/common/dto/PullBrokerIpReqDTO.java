package org.cage.eaglemq.common.dto;

import lombok.Data;

/**
 * ClassName: PullBrokerIpReqDTO
 * PackageName: org.cage.eaglemq.common.dto
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午9:53
 * @Version: 1.0
 */
@Data
public class PullBrokerIpReqDTO extends BaseNameServerRemoteDTO {

    // 根据角色拉取broker 列表
    private String role;

    // 根据 broker 集群name 拉取broker 列表
    private String brokerClusterGroup;
}
