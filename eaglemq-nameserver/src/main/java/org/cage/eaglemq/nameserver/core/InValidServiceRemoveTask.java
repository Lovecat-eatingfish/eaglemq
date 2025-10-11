package org.cage.eaglemq.nameserver.core;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: InValidServiceRemoveTask
 * PackageName: org.cage.eaglemq.nameserver.core
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 下午12:44
 * @Version: 1.0
 */
@Slf4j
public class InValidServiceRemoveTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                log.info("name server 开始执行 健康节点检查任务");

                Map<String, ServiceInstance> serviceInstanceMap = CommonCache.getServiceInstanceManager().getServiceInstanceMap();
                long currentTime = System.currentTimeMillis();
                Iterator<String> iterator = serviceInstanceMap.keySet().iterator();

                while (iterator.hasNext()) {
                    String brokerReqId = iterator.next();
                    ServiceInstance serviceInstance = serviceInstanceMap.get(brokerReqId);
                    // 刚刚注册进来的新的实例
                    if (serviceInstance.getLastHeartBeatTime() == null) {
                        continue;
                    }
                    if (currentTime - serviceInstance.getLastHeartBeatTime() > 3000 * 3) {
                        iterator.remove();
                        log.warn("{} 节点 因为长时间没有发送心跳消息 从而背 name server 移除实例map中", serviceInstance);
                    }
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
