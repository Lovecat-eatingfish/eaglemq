package org.cage.eaglemq.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * @Author idea
 * @Date: Created at 2024/7/25
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumeMsgCommitLogDTO {

    private String fileName;

    private long commitLogOffset;

    private int commitLogSize;

    private byte[] body;

    private int retryTimes;


    @Override
    public String toString() {
        return "ConsumeMsgCommitLogDTO{" +
                "fileName='" + fileName + '\'' +
                ", commitLogOffset=" + commitLogOffset +
                ", commitLogSize=" + commitLogSize +
                ", body=" + new String(this.body) +
                ", retryTimes=" + retryTimes +
                '}';
    }
}
