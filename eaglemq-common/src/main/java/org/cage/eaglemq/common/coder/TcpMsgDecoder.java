package org.cage.eaglemq.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.cage.eaglemq.common.constants.BrokerConstants;

import java.util.List;

/**
 * ClassName: TcpMsgDecoder
 * PackageName: org.cage.eaglemq.common.coder
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:51
 * @Version: 1.0
 */
public class TcpMsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> in) throws Exception {
        int readableBytes = byteBuf.readableBytes();
        // 半包处理
        if (readableBytes > 2 + 4 + 4) {
            if (byteBuf.readShort() != BrokerConstants.DEFAULT_MAGIC_NUM) {
                channelHandlerContext.channel();
                return;
            }
            int code = byteBuf.readInt();
            int len = byteBuf.readInt();
            int readableByteLen = byteBuf.readableBytes();
            // 粘包处理
            if (readableByteLen > len) {
                channelHandlerContext.close();
                return;
            }

            byte[] body = new byte[len];
            byteBuf.readBytes(body);
            TcpMsg tcpMsg = new TcpMsg(code, body);
            in.add(tcpMsg);
        }
    }
}
