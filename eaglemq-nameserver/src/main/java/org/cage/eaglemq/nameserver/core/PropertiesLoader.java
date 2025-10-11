package org.cage.eaglemq.nameserver.core;

import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.common.NameserverProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * ClassName: PropertiesLoader
 * PackageName: org.cage.eaglemq.nameserver.core
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:29
 * @Version: 1.0
 */
public class PropertiesLoader {

    Properties properties = new Properties();

    public void loadProperties() throws IOException {
        String eagleMqHome = System.getenv(BrokerConstants.EAGLE_MQ_HOME);
        String nameServerConfigPath = eagleMqHome + BrokerConstants.NAME_SERVER_CONFIG_PATH;
        properties.load(Files.newInputStream(new File(nameServerConfigPath).toPath()));
        NameserverProperties nameserverProperties = new NameserverProperties();
        nameserverProperties.setNameserverPwd(getStr("nameserver.password"));
        nameserverProperties.setNameserverUser(getStr("nameserver.user"));
        nameserverProperties.setNameserverPort(getInt("nameserver.port"));
        CommonCache.setNameserverProperties(nameserverProperties);
    }

    private String getStr(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("配置参数：" + key + "不存在");
        }
        return value;
    }

    private Integer getInt(String key) {
        return Integer.valueOf(getStr(key));
    }

}
