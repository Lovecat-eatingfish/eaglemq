package org.cage.eaglemq.nameserver.event.listener;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.event.model.SlaveHeartBeatEvent;
import org.cage.eaglemq.nameserver.store.ReplicationChannelManager;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class SlaveHeartBeatListener implements Listener<SlaveHeartBeatEvent> {

    @Override
    public void onReceive(SlaveHeartBeatEvent event) throws Exception {

        log.info("name server的master 接收到接收到从节点心跳信号");

    }
}
