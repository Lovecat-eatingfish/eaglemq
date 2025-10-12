package org.cage.eaglemq.nameserver.common;

import lombok.Data;

/**
 * @author 32782
 */
@Data
public class TraceReplicationProperties {
    // 下一个节点的地址
    private String nextNode;

    // 当前节点启动netty服务端监听的地址
    private Integer port;
}
