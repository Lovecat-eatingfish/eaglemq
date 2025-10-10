package org.cage.eaglemq.broker.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: SpinLock
 * PackageName: org.cage.eaglemq.broker.utils
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 上午11:26
 * @Version: 1.0
 */
public class SpinLock implements PutMessageLock{

    private AtomicInteger atomicInteger = new AtomicInteger(0);
    @Override
    public boolean lock() {
        while (true) {
            if (atomicInteger.compareAndSet(0, 1)) {
                return true;
            }
        }
    }


    // 获取锁子的有多个线程  但是  释放锁子的只有一个
    @Override
    public void unLock() {
        while (true) {
            if (atomicInteger.compareAndSet(1, 9)) {
                return;
            }
        }
    }
}
