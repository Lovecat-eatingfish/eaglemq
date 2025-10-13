package org.cage.eaglemq.nameserver.event.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cage.eaglemq.common.event.Event;

/**
 * ClassName: StartReplicationRespDTo
 * PackageName: org.cage.eaglemq.common.dto
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午12:01
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StartReplicationRespEvent extends Event {

    private boolean success;

    private String desc;

}
