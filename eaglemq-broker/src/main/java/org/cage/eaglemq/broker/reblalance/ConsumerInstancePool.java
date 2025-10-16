package org.cage.eaglemq.broker.reblalance;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.common.utils.AssertUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ConsumerInstancePool {

    private Map<String, List<ConsumerInstance>> consumeInstanceMap = new ConcurrentHashMap<>();
    private static Map<String, IReBalanceStrategy> reBalanceStrategyMap = new HashMap<>();
    private ReBalanceInfo reBalanceInfo = new ReBalanceInfo();

    static {
        reBalanceStrategyMap.put("random", new RandomReBalanceStrategyImpl());
//        reBalanceStrategyMap.put("range", new RandomReBalanceStrategyImpl());
    }

    /**
     * 加入到池中
     *
     * @param consumerInstance
     */
    public void addInstancePool(ConsumerInstance consumerInstance) {
        synchronized (this) {
            String topic = consumerInstance.getTopic();
            //校验topic是否合法
            EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
            AssertUtils.isNotNull(eagleMqTopicModel, "topic非法");
            List<ConsumerInstance> consumerInstanceList = consumeInstanceMap.getOrDefault(topic, new ArrayList<>());
            for (ConsumerInstance instance : consumerInstanceList) {
                if (instance.getConsumerReqId().equals(consumerInstance.getConsumerReqId())) {
                    return;
                }
            }
            consumerInstanceList.add(consumerInstance);
            consumeInstanceMap.put(topic, consumerInstanceList);
            Set<String> consumerGroupSet = reBalanceInfo.getChangeConsumerGroupMap().get(topic);
            if (CollectionUtils.isEmpty(consumerGroupSet)) {
                consumerGroupSet = new HashSet<>();
            }
            consumerGroupSet.add(consumerInstance.getConsumeGroup());
            reBalanceInfo.getChangeConsumerGroupMap().put(topic, consumerGroupSet);
            log.info("new instance add in pool:{}", JSON.toJSONString(consumerInstance));
        }
    }

    /**
     * 从池中移除
     *
     */
    public void removeFromInstancePool(String reqId) {
        synchronized (this) {
            for (String topic : consumeInstanceMap.keySet()) {
                List<ConsumerInstance> consumerInstances = consumeInstanceMap.get(topic);
                List<ConsumerInstance> filterInstances = consumerInstances.stream().filter(item -> !item.getConsumerReqId().equals(reqId)).collect(Collectors.toList());
                //剔除断开的消费者节点
                consumeInstanceMap.put(topic, filterInstances);
            }
            Map<String, Map<String, List<ConsumerInstance>>> consumeHoldMap = CommonCache.getConsumeHoldMap();
            for (String topic : consumeHoldMap.keySet()) {
                Map<String, List<ConsumerInstance>> consumeGroupInstanceMap = consumeHoldMap.get(topic);
                for (String consumeGroup : consumeGroupInstanceMap.keySet()) {
                    List<ConsumerInstance> consumerInstances = consumeGroupInstanceMap.get(consumeGroup);
                    List<ConsumerInstance> filterInstances = consumerInstances.stream().filter(item -> !item.getConsumerReqId().equals(reqId)).collect(Collectors.toList());
                    consumeGroupInstanceMap.put(consumeGroup, filterInstances);
                }
            }
        }
    }

    /**
     * 执行重平衡逻辑
     * 定时任务触发，把已有的队列分配给消费者
     */
    public void doReBalance() {
        synchronized (this) {
            String reBalanceStrategy = CommonCache.getGlobalProperties().getReBalanceStrategy();
            //触发重平衡行为，根据参数决定重平衡策略的不同
            reBalanceInfo.setConsumeInstanceMap(this.consumeInstanceMap);
            reBalanceStrategyMap.get(reBalanceStrategy).doReBalance(reBalanceInfo);
            reBalanceInfo.getChangeConsumerGroupMap().clear();
//            logger.info("do reBalance,{}", JSON.toJSONString(CommonCache.getConsumeHoldMap()));
        }
    }


    public void startReBalanceJob() {
        Thread reBalanceTask = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    doReBalance();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        reBalanceTask.setName("reBalance-task");
        reBalanceTask.start();
    }

}
