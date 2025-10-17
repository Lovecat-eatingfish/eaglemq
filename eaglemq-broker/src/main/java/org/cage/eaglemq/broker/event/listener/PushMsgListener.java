package org.cage.eaglemq.broker.event.listener;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.event.model.PushMsgEvent;
import org.cage.eaglemq.broker.timewheel.DelayMessageDTO;
import org.cage.eaglemq.broker.timewheel.SlotStoreTypeEnum;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.dto.SendMessageToBrokerResponseDTO;
import org.cage.eaglemq.common.dto.TxMessageAckModel;
import org.cage.eaglemq.common.dto.TxMessageDTO;
import org.cage.eaglemq.common.enums.*;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.common.utils.AssertUtils;

import java.io.IOException;

@Slf4j
public class PushMsgListener implements Listener<PushMsgEvent> {

    @Override
    public void onReceive(PushMsgEvent event) throws Exception {
        //消息写入commitLog
        MessageDTO messageDTO = event.getMessageDTO();
        boolean isDelayMsg = messageDTO.getDelay() > 0;
        boolean isHalfMsg = messageDTO.getTxFlag() == TxMessageFlagEnum.HALF_MSG.getCode();
        boolean isRemainHalfAck = messageDTO.getTxFlag() == TxMessageFlagEnum.REMAIN_HALF_ACK.getCode();
        if (isDelayMsg) {
            this.appendDelayMsgHandler(messageDTO, event);
        } else if (isHalfMsg) {
            this.halfMsgHandler(messageDTO, event);
        } else if (isRemainHalfAck) {
            this.remainHalfMsgAckHandler(messageDTO, event);
        } else {
            this.appendDefaultMsgHandler(messageDTO, event);
        }
    }

    /**
     * 普通commitLog消息追加写入
     *
     * @param messageDTO
     * @param event
     */
    private void appendDefaultMsgHandler(MessageDTO messageDTO, PushMsgEvent event) throws IOException {
        CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO, event);
    }

    /**
     * 延迟消息追加写入
     *
     * @param messageDTO
     * @param event
     */
    private void appendDelayMsgHandler(MessageDTO messageDTO, PushMsgEvent event) throws IOException {
        int delaySeconds = messageDTO.getDelay();
        AssertUtils.isTrue(delaySeconds <= 3600, "too large delay seconds");
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setDelay(messageDTO.getDelay());
        delayMessageDTO.setData(messageDTO);
        delayMessageDTO.setSlotStoreType(SlotStoreTypeEnum.DELAY_MESSAGE_DTO);
        delayMessageDTO.setNextExecuteTime(System.currentTimeMillis() + delaySeconds * 1000);
        //延迟消息的下入逻辑
        CommonCache.getTimeWheelModelManager().add(delayMessageDTO);
        //持久化
        MessageDTO delayMessage = new MessageDTO();
        delayMessage.setBody(JSON.toJSONBytes(delayMessageDTO));
        delayMessage.setTopic("delay_queue");
        delayMessage.setQueueId(0);
        delayMessage.setSendWay(MessageSendWay.ASYNC.getCode());
        CommonCache.getCommitLogAppendHandler().appendMsg(delayMessage, event);
        SendMessageToBrokerResponseDTO sendMsgResp = new SendMessageToBrokerResponseDTO();
        sendMsgResp.setMsgId(messageDTO.getMessageId());
        sendMsgResp.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        sendMsgResp.setDesc("send delay msg success");
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.SEND_MSG_RESP.getCode(), JSON.toJSONBytes(sendMsgResp));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }

    private void halfMsgHandler(MessageDTO messageDTO, PushMsgEvent event) {
        TxMessageAckModel txMessageAckModel = new TxMessageAckModel();
        txMessageAckModel.setMessageDTO(messageDTO);
        txMessageAckModel.setChannelHandlerContext(event.getChannelHandlerContext());
        txMessageAckModel.setFirstSendTime(System.currentTimeMillis());
        CommonCache.getTxMessageAckModelMap().put(messageDTO.getMessageId(), txMessageAckModel);
        //时间轮推送 -》
        TxMessageDTO txMessageDTO = new TxMessageDTO();
        txMessageDTO.setMsgId(messageDTO.getMessageId());
        long currentTime = System.currentTimeMillis();
        DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
        delayMessageDTO.setData(txMessageDTO);
        delayMessageDTO.setSlotStoreType(SlotStoreTypeEnum.TX_MESSAGE_DTO);
        delayMessageDTO.setNextExecuteTime(currentTime + 3 * 1000);
        delayMessageDTO.setDelay(3);
        CommonCache.getTimeWheelModelManager().add(delayMessageDTO);
        //告诉客户端写入事务消息成功
        SendMessageToBrokerResponseDTO sendMsgResp = new SendMessageToBrokerResponseDTO();
        sendMsgResp.setMsgId(messageDTO.getMessageId());
        sendMsgResp.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        sendMsgResp.setDesc("send tx half msg success");
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.HALF_MSG_SEND_SUCCESS.getCode(), JSON.toJSONBytes(sendMsgResp));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }


    private void remainHalfMsgAckHandler(MessageDTO messageDTO, PushMsgEvent event) throws IOException {
        LocalTransactionState localTransactionState = LocalTransactionState.of(messageDTO.getLocalTxState());
        if (localTransactionState == LocalTransactionState.COMMIT) {
            CommonCache.getTxMessageAckModelMap().remove(messageDTO.getMessageId());
            CommonCache.getCommitLogAppendHandler().appendMessageToCommitLog(messageDTO);
            log.info("收到事务消息的commit请求");
        } else if (localTransactionState == LocalTransactionState.ROLLBACK) {
            CommonCache.getTxMessageAckModelMap().remove(messageDTO.getMessageId());
            log.info("收到事务消息的rollback请求");
        }
        //告诉客户端写入事务消息成功
        SendMessageToBrokerResponseDTO sendMsgResp = new SendMessageToBrokerResponseDTO();
        sendMsgResp.setMsgId(messageDTO.getMessageId());
        sendMsgResp.setStatus(SendMessageToBrokerResponseStatus.SUCCESS.getCode());
        sendMsgResp.setDesc("send tx remain ack msg success");
        TcpMsg responseMsg = new TcpMsg(BrokerResponseCode.REMAIN_ACK_MSG_SEND_SUCCESS.getCode(), JSON.toJSONBytes(sendMsgResp));
        event.getChannelHandlerContext().writeAndFlush(responseMsg);
    }
}
