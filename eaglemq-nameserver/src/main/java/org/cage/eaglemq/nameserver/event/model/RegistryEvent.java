package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import org.cage.eaglemq.common.enums.RegistryTypeEnum;
import org.cage.eaglemq.common.event.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: RegistryEvent
 * PackageName: org.cage.eaglemq.nameserver.event.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:44
 * @Version: 1.0
 */
@Data
public class RegistryEvent extends Event {
    /**
     * 节点的注册类型，方便统计数据使用
     *
     * @see RegistryTypeEnum
     */
    private String registryType;

    private String user;

    private String password;

    private String ip;

    private Integer port;

    private Map<String, Object> attrs = new HashMap<>();
}
