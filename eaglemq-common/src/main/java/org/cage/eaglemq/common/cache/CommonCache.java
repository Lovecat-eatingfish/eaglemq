package org.cage.eaglemq.common.cache;

import org.cage.eaglemq.common.transaction.TransactionListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: CommonCache
 * PackageName: org.cage.eaglemq.common.cache
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/15 上午12:04
 * @Version: 1.0
 */
public class CommonCache {

    private static Map<String, TransactionListener> transactionListenerMap = new ConcurrentHashMap<>();


    public static Map<String, TransactionListener> getTransactionListenerMap() {
        return transactionListenerMap;
    }

    public static void setTransactionListenerMap(Map<String, TransactionListener> transactionListenerMap) {
        CommonCache.transactionListenerMap = transactionListenerMap;
    }
}
