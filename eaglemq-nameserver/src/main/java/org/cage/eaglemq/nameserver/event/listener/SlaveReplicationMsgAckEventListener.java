package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson2.JSON;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.ServiceRegistryRespDTO;
import org.cage.eaglemq.common.dto.SlaveAckDTO;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.SlaveReplicationMsgAckEvent;

/**
 * ClassName: SlaveReplicationMsgAckEventListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午3:09
 * @Version: 1.0
 */
public class SlaveReplicationMsgAckEventListener implements Listener<SlaveReplicationMsgAckEvent> {
    @Override
    public void onReceive(SlaveReplicationMsgAckEvent event) throws Exception {
        String slaveAckMsgId = event.getMsgId();
        SlaveAckDTO slaveAckDTO = CommonCache.getAckMap().get(slaveAckMsgId);
        if (slaveAckDTO == null) {
            return;
        }
        int currentAckTime = slaveAckDTO.getNeedAckTime().decrementAndGet();
        //如果是复制模式，代表所有从节点已经ack完毕了，
        //如果是半同步复制模式
        if (currentAckTime == 0) {
            CommonCache.getAckMap().remove(slaveAckMsgId);

            String registerMessageId = event.getRegisterMessageId();
            ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
            serviceRegistryRespDTO.setMsgId(registerMessageId);
            slaveAckDTO.getBrokerChannel().writeAndFlush(new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), JSON.toJSONBytes(serviceRegistryRespDTO)));
        }
    }
}
