package org.cage.eaglemq.nameserver.core;

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
import org.cage.eaglemq.nameserver.handler.TcpNettyServerHandler;

/**
 * ClassName: NameServerStarter
 * PackageName: org.cage.eaglemq.nameserver.core
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:45
 * @Version: 1.0
 */
@Slf4j
public class NameServerStarter {

    private final int port;

    public NameServerStarter(int port) {
        this.port = port;
    }

    public void startServer() throws InterruptedException {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ByteBuf delimiter = Unpooled.copiedBuffer(TcpConstants.DEFAULT_DECODE_CHAR.getBytes());
                channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 8, delimiter));
                channel.pipeline().addLast(new TcpMsgDecoder());
                channel.pipeline().addLast(new TcpMsgEncoder());
                channel.pipeline().addLast(new TcpNettyServerHandler(new EventBus("broker-connection-")));
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            log.info("name server is closed");
        }));
        // 阻塞等待简历绑定关系
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        log.info("name server 启动成功， 暴露给broker / producer / consumer的监听端口：{}", port);
        // 阻塞 netty 知道关闭
        channelFuture.channel().closeFuture().sync();
    }

}
