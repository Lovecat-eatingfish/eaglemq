package org.cage.eaglemq.broker.reblalance;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ReBalanceInfo {

    private Map<String, List<ConsumerInstance>> consumeInstanceMap;
    //消费者发生变化的消费组
    private Map<String, Set<String>> changeConsumerGroupMap = new HashMap<>();
}
