package org.cage.eaglemq.nameserver.event.listener;

import com.alibaba.fastjson.JSON;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.PullBrokerIpRespDTO;
import org.cage.eaglemq.common.enums.NameServerResponseCode;
import org.cage.eaglemq.common.enums.RegistryTypeEnum;
import org.cage.eaglemq.common.event.Listener;
import org.cage.eaglemq.nameserver.common.CommonCache;
import org.cage.eaglemq.nameserver.enums.PullBrokerIpRoleEnum;
import org.cage.eaglemq.nameserver.event.model.PullBrokerIpEvent;
import org.cage.eaglemq.nameserver.store.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: PullBrokerIpEventListener
 * PackageName: org.cage.eaglemq.nameserver.event.listener
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/11 上午9:57
 * @Version: 1.0
 */
public class PullBrokerIpEventListener implements Listener<PullBrokerIpEvent> {
    @Override
    public void onReceive(PullBrokerIpEvent event) throws Exception {
        String pullRole = event.getRole();
        List<String> addressList = new ArrayList<>();
        List<String> masterAddressList = new ArrayList<>();
        List<String> slaveAddressList = new ArrayList<>();
        Map<String, ServiceInstance> serviceInstanceMap = CommonCache.getServiceInstanceManager().getServiceInstanceMap();
        for (String reqId : serviceInstanceMap.keySet()) {
            ServiceInstance serviceInstance = serviceInstanceMap.get(reqId);
            if (RegistryTypeEnum.BROKER.getCode().equals(serviceInstance.getRegistryType())) {
                Map<String, Object> brokerAttrs = serviceInstance.getAttrs();
                String group = (String) brokerAttrs.getOrDefault("group", "");
                //先命中集群组，再根据角色进行判断
                if (group.equals(event.getBrokerClusterGroup())) {
                    String role = (String) brokerAttrs.get("role");
                    // 如果他要 master 节点 且 这个实例时 master节点 就加入
                    if (PullBrokerIpRoleEnum.MASTER.getCode().equals(pullRole)
                            && PullBrokerIpRoleEnum.MASTER.getCode().equals(role)) {
                        masterAddressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    } else if (PullBrokerIpRoleEnum.SLAVE.getCode().equals(pullRole)
                            && PullBrokerIpRoleEnum.SLAVE.getCode().equals(role)) {
                        slaveAddressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    } else if (PullBrokerIpRoleEnum.SINGLE.getCode().equals(pullRole)
                            && PullBrokerIpRoleEnum.SINGLE.getCode().equals(role)) {
                        addressList.add(serviceInstance.getIp() + ":" + serviceInstance.getPort());
                    }
                }
            }
        }
        PullBrokerIpRespDTO pullBrokerIpRespDTO = new PullBrokerIpRespDTO();
        pullBrokerIpRespDTO.setMsgId(event.getMsgId());
        pullBrokerIpRespDTO.setMasterAddressList(masterAddressList);
        pullBrokerIpRespDTO.setSlaveAddressList(slaveAddressList);
        //防止ip重复
        pullBrokerIpRespDTO.setAddressList(addressList.stream().distinct().collect(Collectors.toList()));
        event.getChannelHandlerContext().writeAndFlush(new TcpMsg(NameServerResponseCode.PULL_BROKER_ADDRESS_SUCCESS.getCode(),
                JSON.toJSONBytes(pullBrokerIpRespDTO)));
    }
}
