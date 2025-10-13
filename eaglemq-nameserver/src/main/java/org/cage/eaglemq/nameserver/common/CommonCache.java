package org.cage.eaglemq.nameserver.common;

import io.netty.channel.Channel;
import org.cage.eaglemq.common.dto.NodeAckDTO;
import org.cage.eaglemq.common.dto.SlaveAckDTO;
import org.cage.eaglemq.nameserver.core.PropertiesLoader;
import org.cage.eaglemq.nameserver.store.ReplicationChannelManager;
import org.cage.eaglemq.nameserver.store.ReplicationMsgQueueManager;
import org.cage.eaglemq.nameserver.store.ServiceInstanceManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * ClassName: CommonCache
 * PackageName: org.cage.eaglemq.nameserver.common
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:40
 * @Version: 1.0
 */
public class CommonCache {
    // name server 配置文件加载器
    private static PropertiesLoader propertiesLoader = new PropertiesLoader();

    // name server 配置文件实体类
    private static NameserverProperties nameserverProperties = new NameserverProperties();

    // 当前name server 中存存户哦的实例对象管理者
    private static ServiceInstanceManager serviceInstanceManager = new ServiceInstanceManager();

    // 保存 当前连接  master / 链式复制的上一个节点 通信的channel 信息
    private static Channel connectNodeChannel = null;

    // mater 节点管理从节点的handler
    private static ReplicationChannelManager replicationChannelManager = new ReplicationChannelManager();

    // 主从同步的数据管理者
    private static ReplicationMsgQueueManager replicationMsgQueueManager = new ReplicationMsgQueueManager();

    // 链式复制的ack map管理者
    private static Map<String, NodeAckDTO> nodeAckMap = new ConcurrentHashMap<>();

    // 主从复制 ack map管理者
    private static Map<String, SlaveAckDTO> ackMap = new ConcurrentHashMap<>();

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public static void setCountDownLatch(CountDownLatch countDownLatch) {
        CommonCache.countDownLatch = countDownLatch;
    }

    public static Map<String, NodeAckDTO> getNodeAckMap() {
        return nodeAckMap;
    }

    public static void setNodeAckMap(Map<String, NodeAckDTO> nodeAckMap) {
        CommonCache.nodeAckMap = nodeAckMap;
    }

    public static Map<String, SlaveAckDTO> getAckMap() {
        return ackMap;
    }

    public static void setAckMap(Map<String, SlaveAckDTO> ackMap) {
        CommonCache.ackMap = ackMap;
    }

    public static ReplicationMsgQueueManager getReplicationMsgQueueManager() {
        return replicationMsgQueueManager;
    }

    public static void setReplicationMsgQueueManager(ReplicationMsgQueueManager replicationMsgQueueManager) {
        CommonCache.replicationMsgQueueManager = replicationMsgQueueManager;
    }

    public static ReplicationChannelManager getReplicationChannelManager() {
        return replicationChannelManager;
    }

    public static void setReplicationChannelManager(ReplicationChannelManager replicationChannelManager) {
        CommonCache.replicationChannelManager = replicationChannelManager;
    }

    public static Channel getConnectNodeChannel() {
        return connectNodeChannel;
    }

    public static void setConnectNodeChannel(Channel connectNodeChannel) {
        CommonCache.connectNodeChannel = connectNodeChannel;
    }

    public static ServiceInstanceManager getServiceInstanceManager() {
        return serviceInstanceManager;
    }

    public static void setServiceInstanceManager(ServiceInstanceManager serviceInstanceManager) {
        CommonCache.serviceInstanceManager = serviceInstanceManager;
    }

    public static NameserverProperties getNameserverProperties() {
        return nameserverProperties;
    }

    public static void setNameserverProperties(NameserverProperties nameserverProperties) {
        CommonCache.nameserverProperties = nameserverProperties;
    }

    public static PropertiesLoader getPropertiesLoader() {
        return propertiesLoader;
    }

    public static void setPropertiesLoader(PropertiesLoader propertiesLoader) {
        CommonCache.propertiesLoader = propertiesLoader;
    }
}
