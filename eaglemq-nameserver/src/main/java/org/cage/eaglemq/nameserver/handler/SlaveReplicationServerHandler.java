package org.cage.eaglemq.nameserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.event.EventBus;

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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg o) throws Exception {

    }
}
