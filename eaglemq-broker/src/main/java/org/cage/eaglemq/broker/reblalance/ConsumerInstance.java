package org.cage.eaglemq.broker.reblalance;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class ConsumerInstance {

    private String ip;
    private Integer port;
    private String consumeGroup;
    private String topic;
    private Integer batchSize;
    private Set<Integer> queueIdSet = new HashSet<>();
    private String consumerReqId;
}
