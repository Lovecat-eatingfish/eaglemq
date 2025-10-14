package org.cage.eaglemq.client.producer;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.client.netty.BrokerRemoteRespHandler;
import org.cage.eaglemq.common.coder.TcpMsg;
import org.cage.eaglemq.common.dto.*;
import org.cage.eaglemq.common.enums.*;
import org.cage.eaglemq.common.event.EventBus;
import org.cage.eaglemq.common.remote.BrokerNettyRemoteClient;
import org.cage.eaglemq.common.remote.NameServerNettyRemoteClient;
import org.cage.eaglemq.common.utils.AssertUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: DefaultProducerImpl
 * PackageName: org.cage.eaglemq.client.producer
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/14 上午1:31
 * @Version: 1.0
 */
@Slf4j
@Data
public class DefaultProducerImpl implements Producer {

    private String nameServerIp;
    private Integer nameServerPort;
    private String nameServerUser;
    private String nameServerPwd;

    private String brokerClusterGroup;
    private String brokerRole = "single";

    //broker地址会有多个？broker节点会有多个，水平扩展的效果，水平扩展（存储内容会增加，承载压力也会大大增加，节点的选择问题）
    private List<String> brokerAddressList;
    private List<String> masterAddressList;


    private NameServerNettyRemoteClient nameServerNettyRemoteClient;

    // 存储之前和broker建立过的broker客户都
    private Map<String, BrokerNettyRemoteClient> brokerNettyRemoteClientMap = new ConcurrentHashMap<>();


    public void start() throws InterruptedException {
        nameServerNettyRemoteClient = new NameServerNettyRemoteClient(nameServerIp, nameServerPort);
        nameServerNettyRemoteClient.buildNameSererNettyConnection();

        boolean isRegistrySuccess = this.producerRegisterToNameServer();

        if (isRegistrySuccess) {
            this.startHeartBeatTask();
            this.fetchBrokerAddress();
            this.startRefreshBrokerAddressJob();
        }
    }


    public void startRefreshBrokerAddressJob() {
        Thread refreshBrokerAddressJob = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        fetchBrokerAddress();
                    } catch (Exception e) {
                        log.error("refresh broker address job error:", e);
                    }
                }
            }
        });
        refreshBrokerAddressJob.setName("refresh-broker-address-job");
        refreshBrokerAddressJob.start();
    }

    @Override
    public SendResult send(MessageDTO messageDTO) {
        BrokerNettyRemoteClient remoteClient = this.getBrokerNettyRemoteClient();
        String msgId = UUID.randomUUID().toString();
        messageDTO.setMessageId(msgId);
        messageDTO.setSendWay(MessageSendWay.SYNC.getCode());
        TcpMsg tcpMsg = new TcpMsg(BrokerEventCode.PUSH_MSG.getCode(), JSON.toJSONBytes(messageDTO));
        TcpMsg responseMsg = remoteClient.sendSyncMsg(tcpMsg, msgId);
        SendMessageToBrokerResponseDTO sendMessageToBrokerResponseDTO = JSON.parseObject(responseMsg.getBody(), SendMessageToBrokerResponseDTO.class);
        int responseStatus = sendMessageToBrokerResponseDTO.getStatus();
        log.info("producer 给 broker 发送消息的结果：{}", responseStatus);
        SendResult sendResult = new SendResult();
        if (responseStatus == SendMessageToBrokerResponseStatus.SUCCESS.getCode()) {
            sendResult.setSendStatus(SendStatus.SUCCESS);
        } else if (responseStatus == SendMessageToBrokerResponseStatus.FAIL.getCode()) {
            sendResult.setSendStatus(SendStatus.FAIL);
            log.error("producer 给 broker 发送消息的结果：{}", sendMessageToBrokerResponseDTO.getDesc());
        }
        return sendResult;
    }

    private BrokerNettyRemoteClient getBrokerNettyRemoteClient() {
        return new ArrayList<>(this.getBrokerNettyRemoteClientMap().values()).get(0);
    }

    private boolean producerRegisterToNameServer() {
        String messageId = UUID.randomUUID().toString();
        ServiceRegistryReqDTO serviceRegistryReqDTO = new ServiceRegistryReqDTO();
        serviceRegistryReqDTO.setMsgId(messageId);
        serviceRegistryReqDTO.setUser(nameServerUser);
        serviceRegistryReqDTO.setPassword(nameServerPwd);
        serviceRegistryReqDTO.setRegistryType(RegistryTypeEnum.PRODUCER.getCode());
        TcpMsg tcpMsg = new TcpMsg(NameServerEventCode.REGISTRY.getCode(), JSON.toJSONBytes(serviceRegistryReqDTO));
        TcpMsg registryResponse = nameServerNettyRemoteClient.sendSyncMsg(tcpMsg, messageId);
        if (NameServerResponseCode.REGISTRY_SUCCESS.getCode() == registryResponse.getCode()) {
            return true;
        } else {
            log.error("注册账号失败");
            return false;
        }
    }

    private void startHeartBeatTask() {
        Thread heartBeatTask = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        log.info("producer 执行心跳数据发送 给name server master节点");
                        String heartBeatMsgId = UUID.randomUUID().toString();
                        HeartBeatDTO heartBeatDTO = new HeartBeatDTO();
                        heartBeatDTO.setMsgId(heartBeatMsgId);
                        TcpMsg heartBeatResponse = nameServerNettyRemoteClient.sendSyncMsg(new TcpMsg(NameServerEventCode.HEART_BEAT.getCode(),
                                JSON.toJSONBytes(heartBeatDTO)), heartBeatMsgId);
                        log.info("name server 回应 producer 的心跳消息 :{}", JSON.parseObject(heartBeatResponse.getBody()));
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }, "heart-beat-task");
        heartBeatTask.start();
    }


    /**
     * 拉broker地址
     * <p>
     * 主从架构 -》从节点数据 / 主节点数据（两套ip都应该保存下来）
     */
    public void fetchBrokerAddress() {
        String fetchBrokerAddressMsgId = UUID.randomUUID().toString();
        PullBrokerIpDTO pullBrokerIpDTO = new PullBrokerIpDTO();
        if (getBrokerClusterGroup() != null) {
            this.setBrokerRole("master");
            pullBrokerIpDTO.setBrokerClusterGroup(brokerClusterGroup);
        }
        pullBrokerIpDTO.setRole(this.getBrokerRole());
        pullBrokerIpDTO.setMsgId(fetchBrokerAddressMsgId);
        TcpMsg heartBeatResponse = nameServerNettyRemoteClient.sendSyncMsg(new TcpMsg(NameServerEventCode.PULL_BROKER_IP_LIST.getCode(),
                JSON.toJSONBytes(pullBrokerIpDTO)), fetchBrokerAddressMsgId);
        //获取broker节点ip地址，并且缓存起来，可能由多个master-broker角色
        PullBrokerIpRespDTO pullBrokerIpRespDTO = JSON.parseObject(heartBeatResponse.getBody(), PullBrokerIpRespDTO.class);
        this.setBrokerAddressList(pullBrokerIpRespDTO.getAddressList());
        this.setMasterAddressList(pullBrokerIpRespDTO.getMasterAddressList());
        log.info("fetch broker address:{},master:{},slave:{}", this.getBrokerAddressList(), this.getMasterAddressList());
        this.connectBroker();
    }

    /**
     * 连接broker程序
     */
    private void connectBroker() {
        List<String> brokerAddressList = new ArrayList<>();
        if ("single".equals(this.getBrokerRole())) {
            AssertUtils.isNotEmpty(this.getBrokerAddressList(), "broker地址不能为空");
            brokerAddressList = this.getBrokerAddressList();
        } else if ("master".equals(this.getBrokerRole())) {
            AssertUtils.isNotEmpty(this.getMasterAddressList(), "broker地址不能为空");
            brokerAddressList = this.getMasterAddressList();
        }

        //判断之前是否有链接过目标地址，以及链接是否正常，如果链接正常则没必要重新链接，避免无意义的通讯中断情况发生
        List<BrokerNettyRemoteClient> newBrokerNettyRemoteClientList = new ArrayList<>();
        for (String brokerIp : brokerAddressList) {
            BrokerNettyRemoteClient brokerNettyRemoteClient = this.getBrokerNettyRemoteClientMap().get(brokerIp);
            if (brokerNettyRemoteClient == null) {
                //之前没有链接过，需要额外链接接入
                String[] brokerAddressArr = brokerIp.split(":");
                BrokerNettyRemoteClient newBrokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressArr[0],
                        Integer.valueOf(brokerAddressArr[1]));
                newBrokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler(new EventBus("consumer-client-eventbus")));
                //新的链接通道建立
                newBrokerNettyRemoteClientList.add(newBrokerNettyRemoteClient);
                continue;
            }
            //老链接依然需要使用，而且链接顺畅，则继续使用
            if (brokerNettyRemoteClient.isChannelActive()) {
                newBrokerNettyRemoteClientList.add(brokerNettyRemoteClient);
                continue;
            }
            //老链接通讯失败，重连尝试
            String[] brokerAddressArr = brokerIp.split(":");
            BrokerNettyRemoteClient newBrokerNettyRemoteClient = new BrokerNettyRemoteClient(brokerAddressArr[0],
                    Integer.valueOf(brokerAddressArr[1]));
            newBrokerNettyRemoteClient.buildConnection(new BrokerRemoteRespHandler(new EventBus("consumer-client-eventbus")));
            //新的链接通道建立
            newBrokerNettyRemoteClientList.add(newBrokerNettyRemoteClient);
        }


        //需要被关闭的链接过滤出来，进行优雅暂停，然后切换使用新的链接
        List<String> finalBrokerAddressList = brokerAddressList;
        List<String> needRemoveBrokerId = this.getBrokerNettyRemoteClientMap().keySet().stream().filter(reqId -> !finalBrokerAddressList.contains(reqId)).collect(Collectors.toList());
        for (String brokerReqId : needRemoveBrokerId) {
            getBrokerNettyRemoteClientMap().get(brokerReqId).close();
            this.getBrokerNettyRemoteClientMap().remove(brokerReqId);
        }
        this.setBrokerNettyRemoteClientMap(newBrokerNettyRemoteClientList.stream().collect(Collectors.toMap(BrokerNettyRemoteClient::getBrokerReqId, item -> item)));


    }
}
