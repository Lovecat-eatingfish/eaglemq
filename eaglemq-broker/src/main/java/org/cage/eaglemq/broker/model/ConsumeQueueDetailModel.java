package org.cage.eaglemq.broker.model;

import lombok.Data;
import org.cage.eaglemq.broker.utils.ByteConvertUtils;

/**
 * ClassName: ConsumeQueueDetailModel
 * PackageName: org.cage.eaglemq.broker.model
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:17
 * @Version: 1.0
 */
@Data
public class ConsumeQueueDetailModel {

    private int commitLogFileName;

    // 消息在 commit log 文件的索引位置
    private int messageIndex;

    private int messageLength;

    private int retryTime;


    public byte[] convertToBytes() {
        byte[] commitLogFileNameBytes = ByteConvertUtils.intToBytes(commitLogFileName);
        byte[] msgIndexBytes = ByteConvertUtils.intToBytes(messageIndex);
        byte[] msgLengthBytes = ByteConvertUtils.intToBytes(messageLength);
        byte[] retryTimeBytes = ByteConvertUtils.intToBytes(retryTime);
        byte[] finalBytes = new byte[16];
        int p = 0;
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = commitLogFileNameBytes[i];
        }
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = msgIndexBytes[i];
        }
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = msgLengthBytes[i];
        }
        for (int i = 0; i < 4; i++) {
            finalBytes[p++] = retryTimeBytes[i];
        }
        return finalBytes;
    }

    public void buildFromBytes(byte[] body) {
        this.commitLogFileName = ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 0, 4));
        this.messageIndex = ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 4, 4));
        this.messageLength = ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 8, 4));
        this.retryTime = ByteConvertUtils.bytesToInt(ByteConvertUtils.readInPos(body, 12, 4));
    }
}
