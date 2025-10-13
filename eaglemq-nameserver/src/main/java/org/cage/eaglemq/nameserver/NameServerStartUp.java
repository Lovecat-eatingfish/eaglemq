package org.cage.eaglemq.nameserver;

import io.netty.util.internal.StringUtil;
import org.cage.eaglemq.common.enums.ReplicationRoleEnum;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.common.TraceReplicationProperties;
import org.cage.eaglemq.nameserver.core.InValidServiceRemoveTask;
import org.cage.eaglemq.nameserver.core.NameServerStarter;
import org.cage.eaglemq.nameserver.enums.ReplicationModeEnum;
import org.cage.eaglemq.nameserver.replication.*;

import java.io.IOException;

/**
 * ClassName: NameServerStartUp
 * PackageName: org.cage.eaglemq.nameserver
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午12:28
 * @Version: 1.0
 */
public class NameServerStartUp {


    private static ReplicationService replicationService = new ReplicationService();

    private static void initInvalidServerRemoveTask() {
        Thread inValidServiceRemoveTask = new Thread(new InValidServiceRemoveTask());
        inValidServiceRemoveTask.setName("invalid-server-remove-task");
        inValidServiceRemoveTask.start();
    }

    private static void initReplication() {
        //复制逻辑的初始化
        ReplicationModeEnum replicationModeEnum = replicationService.checkProperties();
        //这里面会根据同步模式开启不同的netty进程
        replicationService.startReplicationTask(replicationModeEnum);

        // todo ：完善的地方， 把心跳任务 和 发送同步数据包解耦：现在先把他写道一个里面去
//        if (replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
//
//        }else {
//
//        }
        ReplicationTask replicationTask = null;
        //开启定时任务
        if(replicationModeEnum == ReplicationModeEnum.MASTER_SLAVE) {
            ReplicationRoleEnum roleEnum = ReplicationRoleEnum.of(CommonCache.getNameserverProperties().getMasterSlaveReplicationProperties().getRole());
            if(roleEnum == ReplicationRoleEnum.MASTER) {
                replicationTask = new MasterReplicationMsgSendTask("master-replication-msg-send-task");
                replicationTask.startTaskAsync();
            } else if (roleEnum == ReplicationRoleEnum.SLAVE) {
                //发送链接主节点的请求
                //开启心跳任务，发送给主节点
                replicationTask = new SlaveReplicationHeartBeatTask("slave-replication-heart-beat-send-task");
                replicationTask.startTaskAsync();
            }
        } else if (replicationModeEnum == ReplicationModeEnum.TRACE) {
            //判断当前不是一个尾节点，开启一个复制数据的异步任务
//            TraceReplicationProperties traceReplicationProperties = CommonCache.getNameserverProperties().getTraceReplicationProperties();
//            if(!StringUtil.isNullOrEmpty(traceReplicationProperties.getNextNode())) {
//                replicationTask = new NodeReplicationSendMsgTask("node-replication-msg-send-task");
//                replicationTask.startTaskAsync();
//            }
        }
//        CommonCache.setReplicationTask(replicationTask);

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // 加载 name server 全局的配置文件
        CommonCache.getPropertiesLoader().loadProperties();

        initReplication();

        // 开启 健康检查注册上来的节点的状态 任务
        initInvalidServerRemoveTask();

        // 启动name server
        NameServerStarter nameServerStarter = new NameServerStarter(CommonCache.getNameserverProperties().getNameserverPort());
        nameServerStarter.startServer();
    }
}
