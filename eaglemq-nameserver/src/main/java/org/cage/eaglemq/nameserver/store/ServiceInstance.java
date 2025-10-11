package org.cage.eaglemq.nameserver.store;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ServiceInstance {

    /**
     * 注册类型
     *
     * @see org.cage.eaglemq.common.enums.RegistryTypeEnum
     */
    private String registryType;
    private String ip;
    private Integer port;
    private Long firstRegistryTime;
    private Long lastHeartBeatTime;
    private Map<String, Object> attrs = new HashMap<>();
}
