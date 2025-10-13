package org.cage.eaglemq.nameserver.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.enums.NameServerEventCode;
import org.cage.eaglemq.common.event.Event;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.nameserver.event.model.SlaveHeartBeatEvent;
import org.cage.eaglemq.nameserver.event.model.SlaveReplicationMsgAckEvent;
import org.cage.eaglemq.nameserver.event.model.StartReplicationEvent;

/**
 * @author 32782
 */
@Slf4j
@ChannelHandler.Sharable
public class MasterReplicationServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public MasterReplicationServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {
        log.info("master端收到从节点的消息: {}", tcpMsg);
        int code = tcpMsg.getCode();
        byte[] body = tcpMsg.getBody();
        //从节点发起链接，在master端通过密码验证，建立链接
        Event event = null;
        if (NameServerEventCode.START_REPLICATION.getCode() == code) {
            // 处理slave的连接数据包
            event = JSON.parseObject(body, StartReplicationEvent.class);
        } else if (NameServerEventCode.SLAVE_HEART_BEAT.getCode() == code) {
            // 处理slave的心跳数据包
            event = JSON.parseObject(body, SlaveHeartBeatEvent.class);
        } else if (NameServerEventCode.SLAVE_REPLICATION_ACK_MSG.getCode() == code) {
            event = JSON.parseObject(body, SlaveReplicationMsgAckEvent.class);
        } else {
            return;
        }
        eventBus.publish(event);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有从节点连接到Master: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Master收到原始消息: {}", msg);
        super.channelRead(ctx, msg);
    }
}
