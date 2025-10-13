package org.cage.eaglemq.nameserver.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.nameserver.event.model.ReplicationMsgEvent;

/**
 * @author 32782
 */
@ChannelHandler.Sharable
public class SlaveReplicationServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public SlaveReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        //从节点发起链接，在master端通过密码验证，建立链接
        Event event = null;
        if (NameServerEventCode.MASTER_REPLICATION_MSG.getCode() == code) {
            event = JSON.parseObject(body, ReplicationMsgEvent.class);
        }
        // 这个channel 是 name server 的master节点的channel
        event.setChannelHandlerContext(channelHandlerContext);
        eventBus.publish(event);
    }
}
