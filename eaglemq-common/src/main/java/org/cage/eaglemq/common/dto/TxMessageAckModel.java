package org.cage.eaglemq.common.dto;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public class TxMessageAckModel {


    private MessageDTO messageDTO;

    private ChannelHandlerContext channelHandlerContext;

    private long firstSendTime;
}
