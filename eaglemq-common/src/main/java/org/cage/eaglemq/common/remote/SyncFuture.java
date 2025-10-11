package org.cage.eaglemq.common.remote;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.cage.eaglemq.common.cache.NameServerSyncFutureManager;

import java.util.concurrent.*;

/**
 * ClassName: SyncFuture
 * PackageName: org.cage.eaglemq.common.remote
 * Description: 异步转同步
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午11:16
 * @Version: 1.0
 */
@Data
public class SyncFuture implements Future {

    private Object response;

    private String messageId;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public void setResponse(Object response) {
        this.response = response;
        this.countDownLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        NameServerSyncFutureManager.remove(this.messageId);
        return response;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            countDownLatch.await(timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            NameServerSyncFutureManager.remove(this.messageId);
        }
        return response;
    }

}
