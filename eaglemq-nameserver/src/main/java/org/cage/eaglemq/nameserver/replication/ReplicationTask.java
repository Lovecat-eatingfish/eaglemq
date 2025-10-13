package org.cage.eaglemq.nameserver.replication;

import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: ReplicationTask
 * PackageName: org.cage.eaglemq.nameserver.replication
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午11:09
 * @Version: 1.0
 */
@Slf4j
public abstract class ReplicationTask {

    private String taskName;

    public ReplicationTask(String taskName) {
        this.taskName = taskName;
    }

    public void startTaskAsync() {
        Thread thread = new Thread(this::startTask);
        thread.setName(taskName);
        thread.start();
    }

    public abstract void startTask();
}
