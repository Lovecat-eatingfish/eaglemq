package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.constants.TcpConstants;
import org.cage.eaglemq.common.dto.ServiceRegistryRespDTO;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.RegistryEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;
import org.cage.eaglemq.nameserver.utils.NameserverUtils;

/**
 * ClassName: RegitryListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:45
 * @Version: 1.0
 */
@Slf4j
public class RegistryListener implements Listener<RegistryEvent> {
    @Override
    public void onReceive(RegistryEvent event) throws Exception {
        //安全认证
        boolean isVerify = NameserverUtils.isVerify(event.getUser(), event.getPassword());
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        if (!isVerify) {
            ServiceRegistryRespDTO registryRespDTO = new ServiceRegistryRespDTO();
            registryRespDTO.setMsgId(event.getMsgId());
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    JSON.toJSONBytes(registryRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }

        log.info("注册事件接收:{}", JSON.toJSONString(event));
        channelHandlerContext.attr(AttributeKey.valueOf(TcpConstants.CLIENT_LOG)).set(event.getIp() + ":" + event.getPort());
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(event.getIp());
        serviceInstance.setPort(event.getPort());
        serviceInstance.setRegistryType(event.getRegistryType());
        serviceInstance.setAttrs(event.getAttrs());
        serviceInstance.setFirstRegistryTime(System.currentTimeMillis());
        CommonCache.getServiceInstanceManager().put(serviceInstance);

        String messageId = event.getMsgId();
        ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
        serviceRegistryRespDTO.setMsgId(messageId);
        TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), JSON.toJSONBytes(serviceRegistryRespDTO));
        channelHandlerContext.writeAndFlush(tcpMsg);
    }
}
