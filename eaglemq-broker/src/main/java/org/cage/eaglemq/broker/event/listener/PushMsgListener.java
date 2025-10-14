package org.cage.eaglemq.broker.event.listener;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.event.model.PushMsgEvent;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.enums.TxMessageFlagEnum;
import org.cage.eaglemq.common.event.Listener;

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
//            this.appendDelayMsgHandler(messageDTO, event);
        } else if (isHalfMsg) {
//            this.halfMsgHandler(messageDTO, event);
        } else if (isRemainHalfAck) {
//            this.remainHalfMsgAckHandler(messageDTO, event);
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
}
