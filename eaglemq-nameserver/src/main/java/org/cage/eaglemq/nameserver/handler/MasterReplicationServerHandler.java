package org.cage.eaglemq.nameserver.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.nameserver.event.model.StartReplicationEvent;

/**
 * @author 32782
 */
@ChannelHandler.Sharable
public class MasterReplicationServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public MasterReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        //从节点发起链接，在master端通过密码验证，建立链接
        Event event = null;
        if (NameServerEventCode.START_REPLICATION.getCode() == code) {
            // 处理slave的连接数据包
            event = JSON.parseObject(body, StartReplicationEvent.class);
        }else if (NameServerEventCode.SLAVE_HEART_BEAT.getCode() == code) {
            // 处理slave的心跳数据包
            event = JSON.parseObject(body, StartReplicationEvent.class);
        }else if (NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.getCode() == code) {
            // 处理slave 的消息同步的ack数据包
        }else {
            return;
        }
        eventBus.publish(event);
    }
}
