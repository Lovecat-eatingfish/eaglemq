package org.cage.eaglemq.nameserver.store;

import org.cage.eaglemq.nameserver.event.model.ReplicationMsgEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ReplicationMsgQueueManager {

    private BlockingQueue<ReplicationMsgEvent> replicationMsgQueue = new ArrayBlockingQueue(5000);


    public BlockingQueue<ReplicationMsgEvent> getReplicationMsgQueue() {
        return replicationMsgQueue;
    }

    public ReplicationMsgQueueManager setReplicationMsgQueue(BlockingQueue<ReplicationMsgEvent> replicationMsgQueue) {
        this.replicationMsgQueue = replicationMsgQueue;
        return this;
    }
}

