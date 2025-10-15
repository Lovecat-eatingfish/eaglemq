package org.cage.eaglemq.common.transaction;

import org.cage.eaglemq.common.dto.MessageDTO;
import org.cage.eaglemq.common.enums.LocalTransactionState;

/**
 * @Author idea
 * @Date: Created at 2024/8/18
 * @Description
 */
public interface TransactionListener {

    /**
     * 执行本地事务逻辑处理的回调函数
     *
     * @param messageDTO
     * @return
     */
    LocalTransactionState executeLocalTransaction(final MessageDTO messageDTO);

    /**
     * 事务消息从broker回调到本地的回调接口
     *
     * @param messageDTO
     * @return
     */
    LocalTransactionState callBackHandler(final MessageDTO messageDTO);
}
