package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.constants.TcpConstants;
import org.cage.eaglemq.common.dto.HeartBeatDTO;
import org.cage.eaglemq.common.dto.ServiceRegistryRespDTO;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.enums.ReplicationMsgTypeEnum;
import org.cage.eaglemq.nameserver.event.model.HeartBeatEvent;
import org.cage.eaglemq.nameserver.event.model.ReplicationMsgEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

import java.util.UUID;

/**
 * ClassName: HeartBeatEventListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午9:10
 * @Version: 1.0
 */
@Slf4j
public class HeartBeatEventListener implements Listener<HeartBeatEvent> {
    @Override
    public void onReceive(HeartBeatEvent event) throws Exception {
        ChannelHandlerContext channelHandlerContext = event.getChannelHandlerContext();
        String reqId = (String) channelHandlerContext.attr(AttributeKey.valueOf(TcpConstants.CLIENT_LOG)).get();
        if (reqId == null) {
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(event.getMsgId());
            TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.ERROR_USER_OR_PASSWORD.getCode(),
                    JSON.toJSONBytes(serviceRegistryRespDTO));
            channelHandlerContext.writeAndFlush(tcpMsg);
            channelHandlerContext.close();
            throw new IllegalAccessException("error account to connected!");
        }
        log.info("name server接收到心跳数据包");
        long currentTimestamp = System.currentTimeMillis();
        String[] registerIpAndPort = reqId.split(":");
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setIp(registerIpAndPort[0]);
        serviceInstance.setPort(Integer.valueOf(registerIpAndPort[1]));
        serviceInstance.setLastHeartBeatTime(currentTimestamp);
        CommonCache.getServiceInstanceManager().putIfExist(serviceInstance);

        HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
        heartBeatDTO.setMsgId(event.getMsgId());
        TcpMsg tcpMsg = new TcpMsg(NameServerResponseCode.HEART_BEAT_SUCCESS.getCode(), JSON.toJSONBytes(heartBeatDTO));
        channelHandlerContext.writeAndFlush(tcpMsg);


        // 吸入数据同步对象
        ReplicationMsgEvent replicationMsgEvent = new ReplicationMsgEvent();
        replicationMsgEvent.setServiceInstance(serviceInstance);
        replicationMsgEvent.setMsgId(UUID.randomUUID().toString());
        replicationMsgEvent.setChannelHandlerContext(event.getChannelHandlerContext());
        replicationMsgEvent.setType(ReplicationMsgTypeEnum.HEART_BEAT.getCode());
        CommonCache.getReplicationMsgQueueManager().put(replicationMsgEvent);
    }
}
