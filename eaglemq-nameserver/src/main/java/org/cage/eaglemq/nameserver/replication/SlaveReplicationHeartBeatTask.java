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
        TcpMsg startReplicationResTcpMsg = SyncSendMessageUtils.sendSyncMsg(startReplicationMsg, messageId);
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
                Channel channel = CommonCache.getConnectNodeChannel();
                TcpMsg heartBeatTcpMsg = new TcpMsg(NameServerEventCode.SLAVE_HEART_BEAT.getCode(), JSON.toJSONBytes(new SlaveHeartBeatEvent()));
                channel.writeAndFlush(heartBeatTcpMsg);
                log.info("从节点发送心跳数据给master");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
