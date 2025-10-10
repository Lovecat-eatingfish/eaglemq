package org.cage.eaglemq.broker.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName: ReentrantLock
 * PackageName: org.cage.eaglemq.broker.utils
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午11:24
 * @Version: 1.0
 */
public class UnFailReentrantLock implements PutMessageLock{

    private ReentrantLock reentrantLock  = new ReentrantLock();
    @Override
    public boolean lock() {
        reentrantLock.lock();
        return true;
    }

    @Override
    public void unLock() {
        reentrantLock.unlock();
    }
}
