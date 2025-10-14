package org.cage.eaglemq.broker.netty.nameserver;

import com.alibaba.fastjson.JSON;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.PullBrokerIpDTO;
import org.cage.eaglemq.common.dto.PullBrokerIpRespDTO;
import org.cage.eaglemq.common.dto.ServiceRegistryReqDTO;
import org.cage.eaglemq.common.enums.*;
import org.cage.eaglemq.common.remote.NameServerNettyRemoteClient;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ClassName: NameServerClient
 * PackageName: org.cage.eaglemq.broker.netty.nameserver
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午10:51
 * @Version: 1.0
 */
@Slf4j
public class NameServerClient {

    private NameServerNettyRemoteClient nameServerNettyRemoteClient;

    public NameServerNettyRemoteClient getNameServerNettyRemoteClient() {
        return nameServerNettyRemoteClient;
    }


    // 初始化连接
    public void initConnection() throws InterruptedException {
        String ip = CommonCache.getGlobalProperties().getNameserverIp();
        Integer port = CommonCache.getGlobalProperties().getNameserverPort();
        if (StringUtil.isNullOrEmpty(ip) || port == null || port < 0) {
            throw new RuntimeException("error port or ip");
        }
        nameServerNettyRemoteClient = new NameServerNettyRemoteClient(ip, port);
        nameServerNettyRemoteClient.buildNameSererNettyConnection();
    }

    // 发送注册事件
    public void sendRegistryMsgToNameServer() throws UnknownHostException {
        ServiceRegistryReqDTO registryDTO = new ServiceRegistryReqDTO();
        Map<String, Object> attrs = new HashMap<>();
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String clusterMode = globalProperties.getBrokerClusterMode();

        if (StringUtil.isNullOrEmpty(clusterMode)) {
            attrs.put("role", "single");
        } else if (BrokerClusterModeEnum.MASTER_SLAVE.getCode().equals(clusterMode)) {
            //注册模式是集群架构
            BrokerRegistryRoleEnum brokerRegistryEnum = BrokerRegistryRoleEnum.of(globalProperties.getBrokerClusterRole());
            attrs.put("role", brokerRegistryEnum.getCode());
            attrs.put("group", globalProperties.getBrokerClusterGroup());
        }
        registryDTO.setIp(Inet4Address.getLocalHost().getHostAddress());
        // 这个port 不是真正客户端监听 服务器消息的port  而是一个客户端的标识，存储在服务器里面
        registryDTO.setPort(globalProperties.getBrokerPort());
        registryDTO.setUser(globalProperties.getNameserverUser());
        registryDTO.setPassword(globalProperties.getNameserverPassword());
        registryDTO.setRegistryType(RegistryTypeEnum.BROKER.getCode());
        registryDTO.setAttrs(attrs);
        String messageId = UUID.randomUUID().toString();
        registryDTO.setMsgId(messageId);
        byte[] body = JSON.toJSONBytes(registryDTO);
        //发送注册数据给nameserver
        TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.REGISTRY.getCode(), body);
        TcpMsg registryResponse = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, messageId);
        int code = registryResponse.getCode();
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == code) {
            log.info("broker 注册进入name server 成功");
            CommonCache.getHeartBeatTaskManager().startTask();

        } else if (NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode() == code) {
            //验证失败，抛出异常
            throw new RuntimeException("broker 注册服务器失败， 用户名密码错误， 请重试");
        }
    }


    /**
     * 获取broker主节点的地址
     */
    public String queryBrokerMasterAddress() {
        String clusterMode = CommonCache.getGlobalProperties().getBrokerClusterMode();
        if (!BrokerClusterModeEnum.MASTER_SLAVE.getCode().equals(clusterMode)) {
            return null;
        }
        PullBrokerIpDTO pullBrokerIpDTO = new PullBrokerIpDTO();
        pullBrokerIpDTO.setBrokerClusterGroup(CommonCache.getGlobalProperties().getBrokerClusterGroup());
        pullBrokerIpDTO.setRole(BrokerRegistryRoleEnum.MASTER.getCode());
        pullBrokerIpDTO.setMsgId(UUID.randomUUID().toString());
        TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.PULL_BROKER_IP_LIST.getCode(), JSON.toJSONBytes(pullBrokerIpDTO));
        TcpMsg pullBrokerIpResponse = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, pullBrokerIpDTO.getMsgId());
        PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(pullBrokerIpResponse.getBody(), PullBrokerIpRespDTO.class);
        return pullBrokerIpRespDTO.getMasterAddressList().get(0);
    }
}
