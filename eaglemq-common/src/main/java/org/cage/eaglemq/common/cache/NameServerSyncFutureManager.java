package org.cage.eaglemq.common.cache;

import lombok.Data;
import org.cage.eaglemq.common.remote.SyncFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class NameServerSyncFutureManager {

    private static Map<String, SyncFuture> syncFutureMap = new ConcurrentHashMap<>();

    public static void put(String messageId, SyncFuture syncFuture) {
        syncFutureMap.put(messageId, syncFuture);
    }

    public static SyncFuture get(String messageId) {
        return syncFutureMap.get(messageId);
    }

    public static void remove(String messageId) {
        syncFutureMap.remove(messageId);
    }
}
