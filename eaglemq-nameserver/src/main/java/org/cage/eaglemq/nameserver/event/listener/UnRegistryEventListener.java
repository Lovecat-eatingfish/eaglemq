package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.constants.TcpConstants;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.UnRegistryEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName: UnRegistryEventListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午9:39
 * @Version: 1.0
 */
@Slf4j
public class UnRegistryEventListener implements Listener<UnRegistryEvent> {
    @Override
    public void onReceive(UnRegistryEvent event) throws Exception {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        Object reqId = channelHandlerContext.attr(AttributeKey.valueOf(TcpConstants.CLIENT_LOG)).get();
        if (reqId == null) {
            channelHandlerContext.writeAndFlush(new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    NameServerResponseCode.ERROR_USER_OR_PASSWORD.getDesc().getBytes()));
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        String reqIdStr = (String) reqId;
        //在下线监听器里识别master节点端开的场景
        ServiceInstance needRemoveServiceInstance = CommonCache.getServiceInstanceManager().get(reqIdStr);
        Map<String, Object> attrsMap = needRemoveServiceInstance.getAttrs();
        String brokerClusterRole = (String) attrsMap.getOrDefault("role", "");
        String brokerClusterGroup = (String) attrsMap.getOrDefault("group", "");
        //单机架构不需要处理
        boolean isClusterMode = !StringUtil.isNullOrEmpty(brokerClusterRole) && !StringUtil.isNullOrEmpty(brokerClusterGroup);
        if (isClusterMode) {
            //集群模式+主节点
            if ("master".equals(brokerClusterRole)) {
                log.error("master node fail!");
                List<ServiceInstance> reloadNodeList = new ArrayList<>();
                for (ServiceInstance serviceInstance : CommonCache.getServiceInstanceManager().getServiceInstanceMap().values()) {
                    String matchGroup = (String) serviceInstance.getAttrs().getOrDefault("group", "");
                    String matchRole = (String) serviceInstance.getAttrs().getOrDefault("role", "");
                    if (matchGroup.equals(brokerClusterGroup) && "slave".equals(matchRole)) {
                        reloadNodeList.add(serviceInstance);
                    }
                }
                log.info("get same cluster group slave node:{}", JSON.toJSONString(reloadNodeList));
                //将之前的master节点移除，然后选择从节点中版本号最新的一方作为新的主节点
                long maxVersion = 0L;
                ServiceInstance newMasterServiceInstance = null;
                for (ServiceInstance slaveNode : reloadNodeList) {
                    long latestVersion = (long) slaveNode.getAttrs().getOrDefault("latestVersion", 0L);
                    if (maxVersion <= latestVersion) {
                        newMasterServiceInstance = slaveNode;
                        maxVersion = latestVersion;
                    }
                }
                CommonCache.getServiceInstanceManager().remove(reqIdStr);
                if (newMasterServiceInstance != null) {
                    newMasterServiceInstance.getAttrs().put("role", "master");
                    // todo：通知 其他从节点连接新的 master broker 节点
                    // notifySlavesConnectToNewMaster(newMasterServiceInstance);
                }
                //重新设置主从关系
                CommonCache.getServiceInstanceManager().reload(reloadNodeList);
                log.info("new cluster node is:{}", JSON.toJSONString(newMasterServiceInstance));
            }
        }
        CommonCache.getServiceInstanceManager().remove(reqIdStr);
    }

}

