package org.cage.eaglemq.nameserver.event.model;


import org.cage.eaglemq.common.event.Event;

/**
 * @Author idea
 * @Date: Created in 13:44 2024/5/26
 * @Description
 */
public class SlaveReplicationMsgAckEvent extends Event {

    private String registerMessageId;

    public String getRegisterMessageId() {
        return registerMessageId;
    }

    public SlaveReplicationMsgAckEvent setRegisterMessageId(String registerMessageId) {
        this.registerMessageId = registerMessageId;
        return this;
    }
}
