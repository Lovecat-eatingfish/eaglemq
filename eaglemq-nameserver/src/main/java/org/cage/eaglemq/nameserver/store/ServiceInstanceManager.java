package org.cage.eaglemq.nameserver.store;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceInstanceManager {

    private Map<String, ServiceInstance> serviceInstanceMap = new ConcurrentHashMap<>();

    public Map<String, ServiceInstance> getServiceInstanceMap() {
        return serviceInstanceMap;
    }

    public ServiceInstanceManager setServiceInstanceMap(Map<String, ServiceInstance> serviceInstanceMap) {
        this.serviceInstanceMap = serviceInstanceMap;
        return this;
    }

    public void reload(List<ServiceInstance> serviceInstanceList) {
        serviceInstanceMap.clear();
        for (ServiceInstance serviceInstance : serviceInstanceList) {
            this.put(serviceInstance);
        }
    }

    public void putIfExist(ServiceInstance serviceInstance) {
        ServiceInstance currentInstance = this.get(serviceInstance.getIp(), serviceInstance.getPort());
        if (currentInstance != null && currentInstance.getFirstRegistryTime() != null) {
            currentInstance.setLastHeartBeatTime(serviceInstance.getLastHeartBeatTime());
            serviceInstanceMap.put(serviceInstance.getIp() + ":" + serviceInstance.getPort(), currentInstance);
        } else {
            throw new RuntimeException("之前心跳缓存已经剔除，请重新注册");
        }
    }


    public void put(ServiceInstance serviceInstance) {
        serviceInstanceMap.put(serviceInstance.getIp() + ":" + serviceInstance.getPort(), serviceInstance);
    }

    public ServiceInstance get(String brokerIp, Integer brokerPort) {
        return serviceInstanceMap.get(brokerIp + ":" + brokerPort);
    }

    public ServiceInstance get(String reqId) {
        return serviceInstanceMap.get(reqId);
    }

    public void remove(String reqId) {
        serviceInstanceMap.remove(reqId);
    }

}
