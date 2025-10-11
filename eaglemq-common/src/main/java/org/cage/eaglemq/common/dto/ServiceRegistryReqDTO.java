package org.cage.eaglemq.common.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: ServiceRegisteryReqDTO
 * PackageName: org.cage.eaglemq.common.dto
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:40
 * @Version: 1.0
 */
@Data
public class ServiceRegistryReqDTO extends BaseNameServerRemoteDTO {

    private String ip;

    private int port;

    /**
     * @see org.cage.eaglemq.common.enums.RegistryTypeEnum
     */
    private String registryType;

    private String user;

    private String password;

    private Map<String, Object> attrs = new HashMap<>();

}
