package org.cage.eaglemq.nameserver;

import org.cage.eaglemq.nameserver.common.CommonCache;

import java.io.IOException;

/**
 * ClassName: NameServerStartUp
 * PackageName: org.cage.eaglemq.nameserver
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:28
 * @Version: 1.0
 */
public class NameServerStartUp {
    public static void main(String[] args) throws IOException {
        CommonCache.getPropertiesLoader().loadProperties();
    }
}
