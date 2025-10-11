package org.cage.eaglemq.broker.netty.broker;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsgDecoder;
import org.cage.eaglemq.common.coder.TcpMsgEncoder;
import org.cage.eaglemq.common.constants.TcpConstants;
import org.cage.eaglemq.common.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassName: BrokerServer
 * PackageName: org.cage.eaglemq.broker.netty.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午10:59
 * @Version: 1.0
 */
@Slf4j
public class BrokerServer {


    private int port;

    public BrokerServer(int port) {
        this.port = port;
    }


    /**
     * 启动服务端程序
     *
     * @throws InterruptedException
     */
    public void startServer() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                ch.pipeline().addLast(new TcpMsgDecoder());
                ch.pipeline().addLast(new TcpMsgEncoder());
                ch.pipeline().addLast(new BrokerServerHandler(new EventBus("broker-server-handle")));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("broker is closed");
        }));
        ChannelFuture channelFuture = bootstrap.bind(port).sync();
        log.info("broker client 启动成功:{}", port);
        //阻塞代码
        channelFuture.channel().closeFuture().sync();
    }
}
