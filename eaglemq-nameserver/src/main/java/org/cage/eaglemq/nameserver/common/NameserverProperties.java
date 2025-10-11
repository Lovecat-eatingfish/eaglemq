package org.cage.eaglemq.nameserver.common;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: NameserverProperties
 * PackageName: org.cage.eaglemq.nameserver.common
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:37
 * @Version: 1.0
 */
@Slf4j
@Data
public class NameserverProperties {

    private String nameserverUser;
    private String nameserverPwd;
    private Integer nameserverPort;
}
