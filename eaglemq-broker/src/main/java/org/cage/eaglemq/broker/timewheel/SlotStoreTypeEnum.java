package org.cage.eaglemq.broker.timewheel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.dto.MessageRetryDTO;
import org.cage.eaglemq.common.dto.TxMessageDTO;

@AllArgsConstructor
@Getter
public enum SlotStoreTypeEnum {

    MESSAGE_RETRY_DTO(MessageRetryDTO.class),
    DELAY_MESSAGE_DTO(MessageDTO.class),
    TX_MESSAGE_DTO(TxMessageDTO.class),
    ;
    Class clazz;
}
