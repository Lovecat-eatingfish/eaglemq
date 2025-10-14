package org.cage.eaglemq.nameserver.replication;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.ServiceRegistryRespDTO;
import org.cage.eaglemq.common.dto.SlaveAckDTO;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.common.MasterSlaveReplicationProperties;
import org.cage.eaglemq.nameserver.enums.MasterSlaveReplicationTypeEnum;
import org.cage.eaglemq.nameserver.event.model.ReplicationMsgEvent;
import org.cage.eaglemq.nameserver.store.ReplicationMsgQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: MasterReplicationMsgSendTask
 * PackageName: org.cage.eaglemq.nameserver.replication
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午1:42
 * @Version: 1.0
 * @Description 主从同步专用的数据发送任务
 */
public class MasterReplicationMsgSendTask extends ReplicationTask {

    private static final Logger log = LoggerFactory.getLogger(MasterReplicationMsgSendTask.class);

    public MasterReplicationMsgSendTask(String taskName) {
        super(taskName);
    }

    @Override
    public void startTask() {
        MasterSlaveReplicationProperties masterSlaveReplicationProperties = CommonCache.getNameserverProperties().getMasterSlaveReplicationProperties();
        MasterSlaveReplicationTypeEnum replicationTypeEnum = MasterSlaveReplicationTypeEnum.of(masterSlaveReplicationProperties.getReplicationType());

        //判断当前的复制模式
        //如果是异步复制，直接发送同步数据，同时返回注册成功信号给到broker节点
        //如果是同步复制，发送同步数据给到slave节点，slave节点返回ack信号，主节点收到ack信号后通知给broker注册成功
        //半同步复制其实和同步复制思路很相似

        while (true) {
            try {
                log.info("MasterReplicationMsgSendTask start to send msg to slave");
                ReplicationMsgEvent replicationMsgEvent = CommonCache.getReplicationMsgQueueManager().getReplicationMsgQueue().take();
                // 从事件中获取的就是 broker 发送过来的  channel 就是broker 和  master 的name server 通信的channel
                Channel brokerChannel = replicationMsgEvent.getChannelHandlerContext().channel();

                // 获取所有mater 中的从节点channel
                Map<String, ChannelHandlerContext> channelHandlerContextMap = CommonCache.getReplicationChannelManager().getValidSlaveChannelMap();

                int validSlaveChannelCount = channelHandlerContextMap.keySet().size();
                if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.ASYNC) {
                    this.sendMsgToSlave(replicationMsgEvent);
                    String eventMsgId = replicationMsgEvent.getMsgId();

                    String registerMessageId = CommonCache.getRegisterMessageToReplicationEventIdMapManager().get(eventMsgId);
                    if (registerMessageId == null) {
                        continue;
                    }
                    ServiceRegistryRespDTO serviceRegistryRespDTO = new ServiceRegistryRespDTO();
                    serviceRegistryRespDTO.setMsgId(registerMessageId);
                    brokerChannel.writeAndFlush(new TcpMsg(NameServerResponseCode.REGISTRY_SUCCESS.getCode(), com.alibaba.fastjson2.JSON.toJSONBytes(serviceRegistryRespDTO)));
                } else if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.SYNC) {
                    //需要接收到多少个ack的次数
                    this.inputMsgToAckMap(replicationMsgEvent, validSlaveChannelCount);
                    this.sendMsgToSlave(replicationMsgEvent);
                } else if (replicationTypeEnum == MasterSlaveReplicationTypeEnum.HALF_SYNC) {
                    this.inputMsgToAckMap(replicationMsgEvent, validSlaveChannelCount / 2);
                    this.sendMsgToSlave(replicationMsgEvent);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将主节点需要发送出去的数据注入到一个map中，然后当从节点返回ack的时候，该map的数据会被剔除对应记录
     *
     * @param replicationMsgEvent
     * @param needAckCount
     */
    private void inputMsgToAckMap(ReplicationMsgEvent replicationMsgEvent, int needAckCount) {
        CommonCache.getAckMap().put(replicationMsgEvent.getMsgId(), new SlaveAckDTO(new AtomicInteger(needAckCount), replicationMsgEvent.getChannelHandlerContext()));
    }

    /**
     * 发送数据给到从节点
     *
     * @param replicationMsgEvent
     */
    private void sendMsgToSlave(ReplicationMsgEvent replicationMsgEvent) {
        Map<String, ChannelHandlerContext> channelHandlerContextMap = CommonCache.getReplicationChannelManager().getValidSlaveChannelMap();
        //判断当前采用的同步模式是哪种方式
        for (String reqId : channelHandlerContextMap.keySet()) {
            // 这个事件的channel 一般是 给 非主从节点同步使用的： 给broer 和name server  活着 生产者和消费者和 broiker / name server通信的
            replicationMsgEvent.setChannelHandlerContext(null);
            byte[] body = JSON.toJSONBytes(replicationMsgEvent);
            //异步复制，直接发送给从节点，然后告知broker注册成功
            channelHandlerContextMap.get(reqId).writeAndFlush(new TcpMsg(NameServerEventCode.MASTER_REPLICATION_MSG.getCode(), body));
        }
    }
}
