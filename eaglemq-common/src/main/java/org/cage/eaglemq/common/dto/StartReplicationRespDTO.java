package org.cage.eaglemq.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClassName: StartReplicationRespDTO
 * PackageName: org.cage.eaglemq.common.dto
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/12 下午12:40
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StartReplicationRespDTO extends BaseNameServerReplicationDTO{

    // 是不是成功
    private boolean success;

    private String desc;
}
