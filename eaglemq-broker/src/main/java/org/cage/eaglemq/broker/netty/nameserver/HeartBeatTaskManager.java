package org.cage.eaglemq.broker.netty.nameserver;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.HeartBeatDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.remote.NameServerNettyRemoteClient;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: HeartBeatTaskManager
 * PackageName: org.cage.eaglemq.broker.netty.nameserver
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 下午12:27
 * @Version: 1.0
 */
@Slf4j
public class HeartBeatTaskManager {
    private AtomicInteger flag = new AtomicInteger(0);

    public void startTask() {
        if (flag.getAndIncrement() >= 1) {
            return;
        }
        Thread heartBeatRequestTask = new Thread(new HeartBeatRequestTask());
        heartBeatRequestTask.setName("broker_to_nameserver_heart-beat-request-task");
        heartBeatRequestTask.start();
    }

    private class HeartBeatRequestTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    //心跳包不需要额外透传过多的参数，只需要告诉nameserver这个channel依然存活即可
                    NameServerNettyRemoteClient nameServerNettyRemoteClient = CommonCache.getNameServerClient().getNameServerNettyRemoteClient();
                    HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
                    heartBeatDTO.setMsgId(UUID.randomUUID().toString());
                    TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.HEART_BEAT.getCode(), JSON.toJSONBytes(heartBeatDTO));
                    TcpMsg heartBeatResp = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, heartBeatDTO.getMsgId());
                    if (NameServerResponseCode.HEART_BEAT_SUCCESS.getCode() != heartBeatResp.getCode()) {
                        log.error("heart beat from nameserver is error:{}", heartBeatResp.getCode());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
