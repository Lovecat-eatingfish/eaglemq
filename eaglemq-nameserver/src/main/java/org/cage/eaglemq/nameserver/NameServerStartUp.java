package org.cage.eaglemq.nameserver;

import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.core.InValidServiceRemoveTask;
import org.cage.eaglemq.nameserver.core.NameServerStarter;

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

    private static void initInvalidServerRemoveTask() {
        Thread inValidServiceRemoveTask = new Thread(new InValidServiceRemoveTask());
        inValidServiceRemoveTask.setName("invalid-server-remove-task");
        inValidServiceRemoveTask.start();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        // 加载 name server 全局的配置文件
        CommonCache.getPropertiesLoader().loadProperties();

        // 开启 健康检查任务
        initInvalidServerRemoveTask();

        // 启动name server
        NameServerStarter nameServerStarter = new NameServerStarter(CommonCache.getNameserverProperties().getNameserverPort());
        nameServerStarter.startServer();
    }
}
