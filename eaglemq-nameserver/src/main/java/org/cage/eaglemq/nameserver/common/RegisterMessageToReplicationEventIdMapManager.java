package org.cage.eaglemq.nameserver.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: RegisterMessageToReplicationEventIdMap
 * PackageName: org.cage.eaglemq.nameserver.common
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/14 上午8:59
 * @Version: 1.0
 */
public class RegisterMessageToReplicationEventIdMapManager {

    private  Map<String, String> registerMessageIdMappingEventIdMap = new ConcurrentHashMap<>();

    public  void put(String eventId, String registerMessageId) {
        registerMessageIdMappingEventIdMap.put(eventId, registerMessageId);
    }

    public String get(String eventId) {
        return registerMessageIdMappingEventIdMap.get(eventId);
    }
    public   void remove(String eventId) {
        registerMessageIdMappingEventIdMap.remove(eventId);
    }
}
