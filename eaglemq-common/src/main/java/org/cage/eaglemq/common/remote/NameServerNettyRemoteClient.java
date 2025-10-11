package org.cage.eaglemq.common.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.cache.NameServerSyncFutureManager;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.coder.TcpMsgDecoder;
import org.cage.eaglemq.common.coder.TcpMsgEncoder;
import org.cage.eaglemq.common.constants.TcpConstants;

import java.util.concurrent.ExecutionException;

/**
 * ClassName: NameServerNettyRemote
 * PackageName: org.cage.eaglemq.common.remote
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午10:37
 * @Version: 1.0
 */
@Slf4j
public class NameServerNettyRemoteClient {

    private String ip;
    private int port;
    private Channel channel;

    public NameServerNettyRemoteClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void buildNameSererNettyConnection() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup clientGroup = new NioEventLoopGroup();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                channel.pipeline().addLast(new TcpMsgDecoder());
                channel.pipeline().addLast(new TcpMsgEncoder());
                channel.pipeline().addLast(new NameServerRemoteRespHandler());
            }
        });
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        log.info("broker 连接到 name server 了， nameserver地址：{}", ip + ":" + port);
        this.channel = channelFuture.channel();
    }

    public TcpMsg sendSyncMsg(TcpMsg tcpMsg, String messageId) {
        this.channel.writeAndFlush(tcpMsg);
        SyncFuture syncFuture = new SyncFuture();
        syncFuture.setMessageId(messageId);
        NameServerSyncFutureManager.put(messageId, syncFuture);
        try {
            return (TcpMsg) syncFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
