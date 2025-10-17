package org.cage.eaglemq.broker.event.listener;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.core.CommitLogMMapFileModel;
import org.cage.eaglemq.broker.event.model.TimeWheelEvent;
import org.cage.eaglemq.broker.timewheel.DelayMessageDTO;
import org.cage.eaglemq.broker.timewheel.SlotStoreTypeEnum;
import org.cage.eaglemq.broker.timewheel.TimeWheelSlotModel;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.*;
import org.cage.eaglemq.common.enums.BrokerResponseCode;
import org.cage.eaglemq.common.event.Listener;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TimeWheelListener implements Listener<TimeWheelEvent> {

    @Override
    public void onReceive(TimeWheelEvent event) throws Exception {
        List<TimeWheelSlotModel> timeWheelSlotModelList = event.getTimeWheelSlotModelList();
        if (CollectionUtils.isEmpty(timeWheelSlotModelList)) {
            log.error("timeWheelSlotModelList is empty");
            return;
        }
        for (TimeWheelSlotModel timeWheelSlotModel : timeWheelSlotModelList) {
            if (SlotStoreTypeEnum.MESSAGE_RETRY_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                MessageRetryDTO messageRetryDTO = (MessageRetryDTO) timeWheelSlotModel.getData();
                this.messageRetryHandler(messageRetryDTO);
            } else if (SlotStoreTypeEnum.DELAY_MESSAGE_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                MessageDTO messageDTO = (MessageDTO) timeWheelSlotModel.getData();
                System.out.println("延迟消息重新入commitLog:" + JSON.toJSONString(messageDTO));
                CommonCache.getCommitLogAppendHandler().appendMsg(messageDTO, event);
            } else if (SlotStoreTypeEnum.TX_MESSAGE_DTO.getClazz().equals(timeWheelSlotModel.getStoreType())) {
                TxMessageDTO txMessageDTO = (TxMessageDTO) timeWheelSlotModel.getData();
                //时间轮到期 检查ack缓存是否还有未提交剩余消息ack的记录
                TxMessageAckModel txMessageAckModel = CommonCache.getTxMessageAckModelMap().get(txMessageDTO.getMsgId());
                if (txMessageAckModel == null) {
                    //事务消息已经被ack
                    log.info("txMessageAckModel is already been ack");
                    continue;
                }
                //定时回调客户端行为
                TxMessageCallbackReqDTO txMessageCallbackReqDTO = new TxMessageCallbackReqDTO();
                txMessageCallbackReqDTO.setMessageDTO(txMessageAckModel.getMessageDTO());
                TcpMsg tcpMsg = new TcpMsg(BrokerResponseCode.TX_CALLBACK_MSG.getCode(), JSON.toJSONBytes(txMessageCallbackReqDTO));
                txMessageAckModel.getChannelHandlerContext().writeAndFlush(tcpMsg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            System.out.println("成功发送回调");
                            //重新投递到时间轮里
                            DelayMessageDTO delayMessageDTO = new DelayMessageDTO();
                            delayMessageDTO.setData(txMessageDTO);
                            delayMessageDTO.setSlotStoreType(SlotStoreTypeEnum.TX_MESSAGE_DTO);
                            delayMessageDTO.setNextExecuteTime(System.currentTimeMillis() + 3 * 1000);
                            delayMessageDTO.setDelay(3);
                            CommonCache.getTimeWheelModelManager().add(delayMessageDTO);
                        } else {
                            //可能此时客户端已经挂了
                            System.err.println("异常发送回调");
                        }
                    }
                });
            }
        }
    }

    /**
     * 消息重试处理器
     *
     * @param messageRetryDTO
     */
    private void messageRetryHandler(MessageRetryDTO messageRetryDTO) {
        //将消息数据扔到需要重试的topic里
        CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(messageRetryDTO.getTopic());
        ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(messageRetryDTO.getSourceCommitLogOffset(), messageRetryDTO.getSourceCommitLogSize());
        byte[] commitLogBody = consumeMsgCommitLogDTO.getBody();
        System.out.println("扔到重试topic的数据：" + new String(commitLogBody));
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setBody(commitLogBody);
        messageDTO.setTopic("retry%" + messageRetryDTO.getConsumeGroup());
        messageDTO.setQueueId(ThreadLocalRandom.current().nextInt(3));
        messageDTO.setCurrentRetryTimes(messageRetryDTO.getCurrentRetryTimes() + 1);
        log.info("重试次数增加：{}", messageDTO.getCurrentRetryTimes());
        try {
            CommonCache.getCommitLogAppendHandler().appendMessageToCommitLog(messageDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
