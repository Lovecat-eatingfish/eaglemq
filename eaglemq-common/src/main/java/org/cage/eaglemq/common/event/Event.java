package org.cage.eaglemq.common.event;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * ClassName: Event
 * PackageName: org.cage.eaglemq.common.event
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午1:06
 * @Version: 1.0
 */
@Data
public abstract class Event {

    /**
     * 发送方 给消息的编号
     */
    private String msgId;

    /**
     * 接收消息的服务的channel
     */
    private ChannelHandlerContext channelHandlerContext;
}
