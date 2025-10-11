package org.cage.eaglemq.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.cage.eaglemq.common.constants.TcpConstants;

/**
 * ClassName: TcpMsgEncoder
 * PackageName: org.cage.eaglemq.common.coder
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:51
 * @Version: 1.0
 */
public class TcpMsgEncoder extends MessageToByteEncoder<TcpMsg> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, TcpMsg tcpMsg, ByteBuf out) throws Exception {
        out.writeShort(tcpMsg.getMagic());
        out.writeInt(tcpMsg.getCode());
        out.writeInt(tcpMsg.getLen());
        out.writeBytes(tcpMsg.getBody());
        out.writeBytes(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
    }
}
