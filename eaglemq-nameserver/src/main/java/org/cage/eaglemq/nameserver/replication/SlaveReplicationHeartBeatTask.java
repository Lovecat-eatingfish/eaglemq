package org.cage.eaglemq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.StartReplicationRespDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.SlaveHeartBeatEvent;
import org.cage.eaglemq.nameserver.event.model.StartReplicationEvent;
import org.cage.eaglemq.nameserver.sync.SyncSendMessageUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: SlaveReplicationHeartBeatTask
 * PackageName: org.cage.eaglemq.nameserver.replication
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午11:33
 * @Version: 1.0
 */
@Slf4j
public class SlaveReplicationHeartBeatTask extends ReplicationTask {

    public SlaveReplicationHeartBeatTask(String taskName) {
        super(taskName);
    }

    // asynchrony
    @Override
    public void startTask() {
        String messageId = UUID.randomUUID().toString();
        StartReplicationEvent startReplicationEvent = new StartReplicationEvent();
        startReplicationEvent.setMsgId(messageId);
        startReplicationEvent.setUser(CommonCache.getNameserverProperties().getNameserverUser());
        startReplicationEvent.setPassword(CommonCache.getNameserverProperties().getNameserverPwd());
        TcpMsg startReplicationMsg = new TcpMsg(NameServerEventCode.START_REPLICATION.getCode(), JSON.toJSONBytes(startReplicationEvent));

        // 同步等待  master的结果
        try {
            CommonCache.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 在发送心跳数据前添加连接检查
        Channel channel = CommonCache.getConnectNodeChannel();
        if (channel == null || !channel.isActive()) {
            log.error("与Master节点的连接未建立或已断开");
            return;
        }
        log.info("准备发送START_REPLICATION消息，目标地址: {}",
                CommonCache.getNameserverProperties().getMasterSlaveReplicationProperties().getMaster());
        log.info("消息内容: {}", new String(startReplicationMsg.getBody()));
        TcpMsg startReplicationResTcpMsg = SyncSendMessageUtils.sendSyncMsg(startReplicationMsg, messageId);
        log.info("从节点收到master的开始同步数据响应包: {}", startReplicationResTcpMsg);
        byte[] body = startReplicationResTcpMsg.getBody();
        StartReplicationRespDTO startReplicationRespDTO = JSON.parseObject(body, StartReplicationRespDTO.class);
        boolean success = startReplicationRespDTO.isSuccess();
        if (!success) {
            throw new RuntimeException(startReplicationRespDTO.getDesc());
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                //发送数据给到主节点
                TcpMsg heartBeatTcpMsg = new TcpMsg(NameServerEventCode.SLAVE_HEART_BEAT.getCode(), JSON.toJSONBytes(new SlaveHeartBeatEvent()));
                channel.writeAndFlush(heartBeatTcpMsg);
                log.info("从节点发送心跳数据给master");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
