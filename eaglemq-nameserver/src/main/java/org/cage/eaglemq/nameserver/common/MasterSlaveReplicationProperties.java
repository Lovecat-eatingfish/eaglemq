package org.cage.eaglemq.nameserver.common;

import lombok.Data;
import org.cage.eaglemq.nameserver.enums.MasterSlaveReplicationTypeEnum;

/**
 * @author 32782
 */
@Data
public class MasterSlaveReplicationProperties {

    // 这个 name server集群的 master地址
    private String master;

    // 当前 name server 角色
    private String role;

    /**
     * @see MasterSlaveReplicationTypeEnum
     */
    private String replicationType;

    private Integer port;
}
