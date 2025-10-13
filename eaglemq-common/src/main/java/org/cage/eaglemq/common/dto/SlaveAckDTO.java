package org.cage.eaglemq.common.dto;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 32782
 * 主从同步 master 接收到多少 ack才算发送消息成功
 */
@AllArgsConstructor
@Data
public class SlaveAckDTO {

    /**
     * 需要接收多少个从节点的ack信号
     */
    private AtomicInteger needAckTime;
    /**
     * broker连接主节点的channel
     */
    private ChannelHandlerContext brokerChannel;
}
