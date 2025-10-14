package org.cage.eaglemq.broker.core;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.config.GlobalProperties;
import org.cage.eaglemq.broker.enums.QueueAllocationStrategyEnum;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.strategy.PollingSendMessageStrategy;
import org.cage.eaglemq.broker.strategy.RandomSendMessageStrategy;
import org.cage.eaglemq.broker.strategy.SendMessageStrategy;
import org.cage.eaglemq.common.cache.BrokerServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.dto.SendMessageToBrokerResponseDTO;
import org.cage.eaglemq.common.dto.SlaveSyncRespDTO;
import org.cage.eaglemq.common.enums.*;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.remote.SyncFuture;

import java.io.IOException;
import java.util.Objects;

/**
 * ClassName: CommitLogAppendHandler
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:46
 * @Version: 1.0
 */
@Slf4j
public class CommitLogAppendHandler {

    public void prepareMMapLoading(String topicName) throws IOException {
        CommitLogMMapFileModel mapFileModel = new CommitLogMMapFileModel();
        GlobalProperties globalProperties = CommonCache.getGlobalProperties();
        String brokerQueueAllocationStrategy = globalProperties.getBrokerQueueAllocationStrategy();
        SendMessageStrategy strategy = null;
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        if (Objects.equals(brokerQueueAllocationStrategy, QueueAllocationStrategyEnum.POLLING.getStrategy())) {
            strategy = new PollingSendMessageStrategy(eagleMqTopicModel);
        } else {
            strategy = new RandomSendMessageStrategy(eagleMqTopicModel);
        }
        log.info("queueId 的分配策略是：{}", strategy);
        mapFileModel.loadFileInMMap(topicName, 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE, strategy);
        CommonCache.getCommitLogMMapFileModelManager().put(topicName, mapFileModel);
    }

    // 进行broker的主从同步  需要发布事件
    public void appendMsg(MessageDTO messageDTO, Event event) throws IOException {
        CommonCache.getCommitLogAppendHandler().appendMessageToCommitLog(messageDTO);
        //判断下是主节点还是从节点
        int sendWay = messageDTO.getSendWay();
        boolean isAsyncSend = MessageSendWay.ASYNC.getCode() == sendWay;
        boolean isClusterMode = BrokerClusterModeEnum.MASTER_SLAVE.getCode().equals(CommonCache.getGlobalProperties().getBrokerClusterMode());
        boolean isMasterNode = "master".equals(CommonCache.getGlobalProperties().getBrokerClusterRole());
        boolean isDelayMsg = messageDTO.getDelay() > 0;

        if (isClusterMode) {
            // 集群模式 broker 处理方式master
            if (isMasterNode) {
                //主节点 发送同步请求给从节点,异步发送是没有消息id的
                for (ChannelHandlerContext slaveChannel : CommonCache.getSlaveChannelMap().values()) {
                    slaveChannel.writeAndFlush(new TcpMsg(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(messageDTO)));
                }
                SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
                sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
                sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMessageId());
                TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
                event.getChannelHandlerContext().writeAndFlush(responseMsg);
//                if (isAsyncSend || isDelayMsg) {
//                    return;
//                }
//                SyncFuture syncFuture = new SyncFuture();
//                syncFuture.setMessageId(messageDTO.getMessageId());
//                BrokerServerSyncFutureManager.put(messageDTO.getMessageId(), syncFuture);
            } else {
                // 集群模式 broker slave 的处理模式
                if (isAsyncSend || isDelayMsg) {
                    return;
                }
                //从节点 返回响应code给主节点
                SlaveSyncRespDTO slaveSyncAckRespDTO = new SlaveSyncRespDTO();
                slaveSyncAckRespDTO.setSyncSuccess(true);
                slaveSyncAckRespDTO.setMsgId(messageDTO.getMessageId());
                event.getChannelHandlerContext().writeAndFlush(new TcpMsg(BrokerResponseCode.SLAVE_SYNC_RESP.getCode(),
                        JSON.toJSONBytes(slaveSyncAckRespDTO)));
            }
        } else {
            //单机版本处理逻辑
            if (isAsyncSend || isDelayMsg) {
                return;
            }
            SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = new SendMessageToBrokerResponseDTO();
            sendMessageToBrokerResponseDTO.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
            sendMessageToBrokerResponseDTO.setMsgId(messageDTO.getMessageId());
            TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMessageToBrokerResponseDTO));
            event.getChannelHandlerContext().writeAndFlush(responseMsg);
        }
    }

    public void appendMessageToCommitLog(MessageDTO messageDTO) throws IOException {
        CommitLogMMapFileModel mapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(messageDTO.getTopic());
        if (mapFileModel == null) {
            throw new RuntimeException("topic is invalid!");
        }
        mapFileModel.writeContent(messageDTO, true);
    }
}
