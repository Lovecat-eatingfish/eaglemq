package org.cage.eaglemq.broker.config;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.common.constants.BrokerConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * ClassName: GlobalPropertiesLoader
 * PackageName: org.cage.eaglemq.broker.config
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午8:58
 * @Version: 1.0
 */
@Slf4j
public class GlobalPropertiesLoader {

    public void loadProperties() throws IOException {
        GlobalProperties globalProperties = new GlobalProperties();
        String eagleMqHome = System.getenv(BrokerConstants.EAGLE_MQ_HOME);
        log.info("eagleMQHome: {}", eagleMqHome);
        if (StringUtil.isNullOrEmpty(eagleMqHome)) {
            throw new IllegalArgumentException("EAGLE_MQ_HOME is null");
        }
        globalProperties.setEagleMqHome(eagleMqHome);

        String brokerPropertiesPath = eagleMqHome + BrokerConstants.BROKER_PROPERTIES_PATH;
        Properties properties = new Properties();
        properties.load(Files.newInputStream(new File(brokerPropertiesPath).toPath()));

        // todo 读取其他属性
        CommonCache.setGlobalProperties(globalProperties);
    }
}
