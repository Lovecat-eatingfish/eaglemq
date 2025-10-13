package org.cage.eaglemq.nameserver.replication;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.coder.TcpMsgDecoder;
import org.cage.eaglemq.common.coder.TcpMsgEncoder;
import org.cage.eaglemq.common.enums.ReplicationRoleEnum;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.utils.AssertUtils;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.common.MasterSlaveReplicationProperties;
import org.cage.eaglemq.nameserver.common.NameserverProperties;
import org.cage.eaglemq.nameserver.common.TraceReplicationProperties;
import org.cage.eaglemq.nameserver.enums.ReplicationModeEnum;
import org.cage.eaglemq.nameserver.handler.MasterReplicationServerHandler;
import org.cage.eaglemq.nameserver.handler.SlaveReplicationServerHandler;
import org.cage.eaglemq.nameserver.sync.SlaveNameServerRemoteRespHandler;

/**
 * ClassName: ReplicationService
 * PackageName: org.cage.eaglemq.nameserver.replication
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 上午10:38
 * @Version: 1.0
 */
@Slf4j
public class ReplicationService {

    // 主从复制 节点启动的参数校验
    public ReplicationModeEnum checkProperties() {
        NameserverProperties nameserverProperties = CommonCache.getNameserverProperties();
        String mode = nameserverProperties.getReplicationMode();
        if (StringUtil.isNullOrEmpty(mode)) {
            log.info("执行单机模式");
            return null;
        }
        //为空，参数不合法，抛异常
        ReplicationModeEnum replicationModeEnum = ReplicationModeEnum.of(mode);
        AssertUtils.isNotNull(replicationModeEnum, "复制模式参数异常");
        if (replicationModeEnum == ReplicationModeEnum.TRACE) {
            //链路复制
            TraceReplicationProperties traceReplicationProperties = nameserverProperties.getTraceReplicationProperties();
            AssertUtils.isNotNull(traceReplicationProperties.getPort(), "node节点的端口为空");
        } else {
            //主从复制
            MasterSlaveReplicationProperties masterSlaveReplicationProperties = nameserverProperties.getMasterSlaveReplicationProperties();
            AssertUtils.isNotBlank(masterSlaveReplicationProperties.getMaster(), "master参数不能为空");
            AssertUtils.isNotBlank(masterSlaveReplicationProperties.getRole(), "role参数不能为空");
            AssertUtils.isNotBlank(masterSlaveReplicationProperties.getReplicationType(), "主从同步类型参数不能为空");
            AssertUtils.isNotNull(masterSlaveReplicationProperties.getPort(), "同步端口不能为空");
        }
        return replicationModeEnum;
    }

    // 更具不同的角色和 不同的同步方式 开启不同的netty进程等
    public void startReplicationTask(ReplicationModeEnum replicationModeEnum) {
        //单机版本，不用做处理
        if (replicationModeEnum == null) {
            return;
        }
        int port = 0;
        NameserverProperties nameserverProperties = CommonCache.getNameserverProperties();
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            port = nameserverProperties.getMasterSlaveReplicationProperties().getPort();
        } else {
            replicationModeEnum = ReplicationModeEnum.TRACE;
        }

        // 确定角色
        ReplicationRoleEnum roleEnum;
        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            roleEnum = ReplicationRoleEnum.of(nameserverProperties.getMasterSlaveReplicationProperties().getRole());
        } else {
            String nextNode = nameserverProperties.getTraceReplicationProperties().getNextNode();
            if (StringUtil.isNullOrEmpty(nextNode)) {
                roleEnum = ReplicationRoleEnum.TAIL_NODE;
            } else {
                roleEnum = ReplicationRoleEnum.NODE;
            }
            port = nameserverProperties.getTraceReplicationProperties().getPort();
        }

        int replicationPort = port;
        //master角色，开启netty进程同步数据给master
        if (roleEnum == ReplicationRoleEnum.MASTER) {
            startNettyServerAsync(new MasterReplicationServerHandler(new EventBus("master-replication-task-")), replicationPort);
        } else if (roleEnum == ReplicationRoleEnum.SLAVE) {
            //slave角色，主动连接master角色
            String masterAddress = nameserverProperties.getMasterSlaveReplicationProperties().getMaster();
            startNettyConnAsync(new SlaveReplicationServerHandler(new EventBus("slave-replication-task-")), masterAddress);
        }
//        else if (roleEnum == ReplicationRoleEnum.NODE) {
//            String nextNodeAddress = nameserverProperties.getTraceReplicationProperties().getNextNode();
//            startNettyServerAsync(new NodeWriteMsgReplicationServerHandler(new EventBus("node-write-msg-replication-task-")), replicationPort);
//            startNettyConnAsync(new NodeSendReplicationMsgServerHandler(new EventBus("node-send-replication-msg-task-")), nextNodeAddress);
//        } else if (roleEnum == ReplicationRoleEnum.TAIL_NODE) {
//            startNettyServerAsync(new NodeWriteMsgReplicationServerHandler(new EventBus("node-write-msg-replication-task-")), replicationPort);
//        }

    }

    /**
     * 异步开启一个netty的进程
     *
     * @param simpleChannelInboundHandler
     * @param port
     */
    private void startNettyServerAsync(SimpleChannelInboundHandler<TcpMsg> simpleChannelInboundHandler, int port) {
        Thread nettyServerTask = new Thread(() -> {
            //负责netty启动
            NioEventLoopGroup bossGroup = new NioEventLoopGroup();
            //处理网络io中的read&write事件
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new TcpMsgDecoder());
                    ch.pipeline().addLast(new TcpMsgEncoder());
                    ch.pipeline().addLast(simpleChannelInboundHandler);
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                log.info("nameserver's replication application is closed");
            }));
            ChannelFuture channelFuture = null;
            try {
                //master-slave架构
                //写入数据的节点，这里就会开启一个服务
                //非写入数据的节点，这里就需要链接一个服务
                //trace架构
                //又要接收外界数据，又要复制数据给外界
                channelFuture = bootstrap.bind(port).sync();
                log.info("start nameserver's replication application on port: {}", port);
                //阻塞代码
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        nettyServerTask.start();
    }


    /**
     * 异步开启对目标进程的链接
     *
     * @param simpleChannelInboundHandler
     * @param address
     */
    private void startNettyConnAsync(SimpleChannelInboundHandler simpleChannelInboundHandler, String address) {

        Thread nettyConnTask = new Thread(() -> {
            EventLoopGroup clientGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            Channel channel;
            bootstrap.group(clientGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TcpMsgDecoder());
                    ch.pipeline().addLast(new TcpMsgEncoder());
                    ch.pipeline().addLast(new SlaveNameServerRemoteRespHandler(new EventBus("name server nettyAsync ——》nettySync thread")));
                    ch.pipeline().addLast(simpleChannelInboundHandler);
                }
            });
            ChannelFuture channelFuture = null;
            try {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    clientGroup.shutdownGracefully();
                    log.info("nameserver's replication connect application is closed");
                }));
                String[] addr = address.split(":");
                log.info("nameserver's replication connect application on address: {}", address);
                channelFuture = bootstrap.connect(addr[0], Integer.parseInt(addr[1])).sync();
                if (!channelFuture.isSuccess()) {
                    log.error("连接Master节点失败", channelFuture.cause());
                    return;
                }
                //连接了master节点的channel对象，建议保存
                channel = channelFuture.channel();
                CommonCache.setConnectNodeChannel(channel);
                CommonCache.getCountDownLatch().countDown();
                log.info("name server 从节点连接到 name server master 节点");
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        nettyConnTask.start();
    }

}
