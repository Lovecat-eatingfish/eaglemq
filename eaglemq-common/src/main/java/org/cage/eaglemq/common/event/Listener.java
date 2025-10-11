package org.cage.eaglemq.common.event;

/**
 * ClassName: Listener
 * PackageName: org.cage.eaglemq.common.event
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:11
 * @Version: 1.0
 */
public interface Listener<E>{


    /**
     * 回调通知
     *
     * @param event
     */
    void onReceive(E event) throws Exception;
}
