package org.cage.eaglemq.broker.netty.broker;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.event.EventBus;

/**
 * ClassName: BrokerServerHandler
 * PackageName: org.cage.eaglemq.broker.netty.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午11:00
 * @Version: 1.0
 */
@ChannelHandler.Sharable
@Slf4j
public class BrokerServerHandler extends SimpleChannelInboundHandler<TcpMsg> {

    private EventBus eventBus;

    public BrokerServerHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.init();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg) throws Exception {

    }
}
