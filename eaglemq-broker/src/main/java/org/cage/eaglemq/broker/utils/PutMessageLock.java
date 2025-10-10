package org.cage.eaglemq.broker.utils;

/**
 * ClassName: PutMessageLock
 * PackageName: org.cage.eaglemq.broker.utils
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午11:24
 * @Version: 1.0
 */
public interface PutMessageLock {

    boolean lock();

    void unLock();
}
