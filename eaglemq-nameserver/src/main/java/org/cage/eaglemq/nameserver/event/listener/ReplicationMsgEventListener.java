package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.ReplicationMsgEvent;
import org.cage.eaglemq.nameserver.event.model.SlaveReplicationMsgAckEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

/**
 * ClassName: ReplicationMsgEventListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午3:07
 * @Version: 1.0
 */
public class ReplicationMsgEventListener implements Listener<ReplicationMsgEvent> {
    @Override
    public void onReceive(ReplicationMsgEvent event) throws Exception {
        ServiceInstance serviceInstance = event.getServiceInstance();
        //从节点接收主节点同步数据逻辑
        CommonCache.getServiceInstanceManager().put(serviceInstance);
        SlaveReplicationMsgAckEvent slaveReplicationMsgAckEvent = new SlaveReplicationMsgAckEvent();
        slaveReplicationMsgAckEvent.setMsgId(event.getMsgId());
        event.getChannelHandlerContext().channel().writeAndFlush(new TcpMsg(NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.ordinal(),
                JSON.toJSONBytes(slaveReplicationMsgAckEvent)));
    }
}
