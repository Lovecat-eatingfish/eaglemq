package org.cage.eaglemq.broker.slave;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.StartSyncReqDTO;
import org.cage.eaglemq.common.dto.StartSyncRespDTO;
import org.cage.eaglemq.common.enums.BrokerEventCode;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.BrokerNettyRemoteClient;

import java.util.UUID;

/**
 * @Description 从节点同步服务
 */
@Slf4j
public class SlaveSyncService {

    private BrokerNettyRemoteClient brokerNettyRemoteClient;

    public boolean connectMasterBrokerNode(String address) {
        String[] addressAddr = address.split(":");
        String ip = addressAddr[0];
        Integer port = Integer.valueOf(addressAddr[1]);
        try {
            brokerNettyRemoteClient = new BrokerNettyRemoteClient(ip, port);
            brokerNettyRemoteClient.buildConnection(new SlaveSyncServerHandler(new EventBus("slave-sync-eventbus")));
            return true;
        } catch (Exception e) {
            log.error("error connect master broker", e);
        }
        return false;
    }

    public void sendStartSyncMsg() {
        StartSyncReqDTO startSyncReqDTO = new StartSyncReqDTO();
        startSyncReqDTO.setMsgId(UUID.randomUUID().toString());
        TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.START_SYNC_MSG.getCode(), JSON.toJSONBytes(startSyncReqDTO));
        TcpMsg startSyncMsgResp = brokerNettyRemoteClient.sendSyncMsg(tcpMsg, startSyncReqDTO.getMsgId());
        StartSyncRespDTO startSyncRespDTO = JSON.parseObject(startSyncMsgResp.getBody(), StartSyncRespDTO.class);
        if (!startSyncRespDTO.isSuccess()) {
            throw new RuntimeException("连接 master 节点 进行主从同步 拒绝");
        }

        log.info("slave 的broker 可以开始进行主从同步了");
    }
}
