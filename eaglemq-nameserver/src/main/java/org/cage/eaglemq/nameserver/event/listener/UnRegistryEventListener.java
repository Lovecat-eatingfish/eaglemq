package org.cage.eaglemq.nameserver.event.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.constants.TcpConstants;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.UnRegistryEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

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
        // 单机name server
        // todo :需要考虑name server 集群
        if (true) {
            CommonCache.getServiceInstanceManager().remove(reqIdStr);
        } else {
            // todo:
            ServiceInstance needRemoveServiceInstance = CommonCache.getServiceInstanceManager().get(reqIdStr);
            Map<String, Object> attrsMap = needRemoveServiceInstance.getAttrs();
            String brokerClusterRole = (String) attrsMap.getOrDefault("role", "");
            String brokerClusterGroup = (String) attrsMap.getOrDefault("group", "");
        }

    }
}
