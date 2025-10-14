package org.cage.eaglemq.nameserver.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.HeartBeatDTO;
import org.cage.eaglemq.common.dto.PullBrokerIpReqDTO;
import org.cage.eaglemq.common.dto.ServiceRegistryReqDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.nameserver.event.model.HeartBeatEvent;
import org.cage.eaglemq.nameserver.event.model.PullBrokerIpEvent;
import org.cage.eaglemq.nameserver.event.model.RegistryEvent;
import org.cage.eaglemq.nameserver.event.model.UnRegistryEvent;

import java.net.InetSocketAddress;

@Slf4j
@ChannelHandler.Sharable
public class TcpNettyServerHandler extends SimpleChannelInboundHandler<TcpMsg> {
    private EventBus eventBus;

    public TcpNettyServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
//        log.info("name server 服务节点：{} 收到消息：{}", channelHandlerContext.channel().remoteAddress(), tcpMsg);
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        Event event = null;
        if (NameServerEventCode.REGISTRY.getCode() == code) {
            ServiceRegistryReqDTO serviceRegistryReqDTO = JSON.parseObject(body, ServiceRegistryReqDTO.class);
            RegistryEvent registryEvent = new RegistryEvent();
            registryEvent.setMsgId(serviceRegistryReqDTO.getMsgId());
            registryEvent.setPassword(serviceRegistryReqDTO.getPassword());
            registryEvent.setUser(serviceRegistryReqDTO.getUser());
            registryEvent.setAttrs(serviceRegistryReqDTO.getAttrs());
            registryEvent.setRegistryType(serviceRegistryReqDTO.getRegistryType());

            // 补充注册者的 ip 和 port
            if (StringUtil.isNullOrEmpty(serviceRegistryReqDTO.getIp())) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
                registryEvent.setPort(inetSocketAddress.getPort());
                registryEvent.setIp(inetSocketAddress.getHostString());
            } else {
                registryEvent.setPort(serviceRegistryReqDTO.getPort());
                registryEvent.setIp(serviceRegistryReqDTO.getIp());
            }
            event = registryEvent;
        } else if (NameServerEventCode.HEART_BEAT.getCode() == code) {
            HeartBeatDTO heartBeatDTO = JSON.parseObject(body, HeartBeatDTO.class);
            HeartBeatEvent heartBeatEvent = new HeartBeatEvent();
            heartBeatEvent.setMsgId(heartBeatDTO.getMsgId());
            heartBeatEvent.setMsgId(heartBeatDTO.getMsgId());
            event = heartBeatEvent;
        } else if (NameServerEventCode.PULL_BROKER_IP_LIST.getCode() == code) {
            PullBrokerIpReqDTO pullBrokerIpReqDTO = JSON.parseObject(body, PullBrokerIpReqDTO.class);
            PullBrokerIpEvent pullBrokerIpEvent = new PullBrokerIpEvent();
            pullBrokerIpEvent.setMsgId(pullBrokerIpReqDTO.getMsgId());
            String role = pullBrokerIpReqDTO.getRole();
            pullBrokerIpEvent.setRole(role);
            pullBrokerIpEvent.setBrokerClusterGroup(pullBrokerIpReqDTO.getBrokerClusterGroup());
            event = pullBrokerIpEvent;
        } else if (NameServerEventCode.UN_REGISTRY.getCode() == code) {
            UnRegistryEvent unRegistryEvent = new UnRegistryEvent();
            unRegistryEvent.setChannelHandlerContext(channelHandlerContext);
            event = unRegistryEvent;
        } else {
            TcpMsg responseTcpMsg = new TcpMsg(NameServerResponseCode.NOT_EXISTS_MESSAGE_TYPE.getCode(), NameServerResponseCode.NOT_EXISTS_MESSAGE_TYPE.getDesc().getBytes());
            channelHandlerContext.writeAndFlush(responseTcpMsg);
            return;
        }
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }

    // 这里比 使用心跳检查更快 更新name server 实例map的变化。
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        UnRegistryEvent unRegistryEvent = new UnRegistryEvent();
        unRegistryEvent.setChannelHandlerContext(ctx);
        eventBus.publish(unRegistryEvent);
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 连接成功", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
}
