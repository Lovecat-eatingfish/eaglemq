package org.cage.eaglemq.common.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.constants.TcpConstants;

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
        // 检查是否至少有magic number + code + len的字节数
        if (byteBuf.readableBytes() < 2 + 4 + 4) {
            // 可读字节数不足，等待更多数据
            return;
        }

        // 标记当前读位置，以便在数据不完整时回退
        byteBuf.markReaderIndex();

        // 读取magic number
        if (byteBuf.readShort() != BrokerConstants.DEFAULT_MAGIC_NUM) {
            // magic number不匹配，丢弃数据
            channelHandlerContext.close();
            return;
        }

        int code = byteBuf.readInt();
        int len = byteBuf.readInt();

        // 注意：需要考虑编码器添加的分隔符长度
        int delimiterLen = TcpConstants.DEFAULT_DECODE_CHAR.getBytes().length;

        // 检查是否有足够的字节读取body和分隔符
        if (byteBuf.readableBytes() < len + delimiterLen) {
            // 数据不完整，回退读位置，等待更多数据
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] body = new byte[len];
        byteBuf.readBytes(body);

        // 跳过分隔符
        byteBuf.skipBytes(delimiterLen);

        TcpMsg tcpMsg = new TcpMsg(code, body);
        in.add(tcpMsg);
    }
}
