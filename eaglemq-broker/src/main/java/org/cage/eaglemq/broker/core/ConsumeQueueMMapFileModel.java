package org.cage.eaglemq.broker.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.model.EagleMqTopicModel;
import org.cage.eaglemq.broker.model.QueueModel;
import org.cage.eaglemq.broker.utils.LogFileNameUtil;
import org.cage.eaglemq.broker.utils.PutMessageLock;
import org.cage.eaglemq.broker.utils.UnFailReentrantLock;
import org.cage.eaglemq.common.constants.BrokerConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ConsumeQueueMMapFileModel
 * PackageName: org.cage.eaglemq.broker.core
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午2:27
 * @Version: 1.0
 */
@Slf4j
public class ConsumeQueueMMapFileModel {

    private MappedByteBuffer mappedByteBuffer;

    private ByteBuffer readBuffer;

    private String topic;

    // 可以添加一个字段记录写入位置
    private int wrotePosition = 0;

    @Getter
    private Integer queueId;

    private String consumeQueueFileName;

    private PutMessageLock putMessageLock;

    /**
     * 指定offset做文件的映射
     *
     * @param topicName         消息主题
     * @param queueId           队列id
     * @param startOffset       开始映射的offset
     * @param latestWriteOffset 最新写入的offset
     * @param mappedSize        映射的体积 (byte)
     */
    public void loadFileInMMap(String topicName, Integer queueId, int startOffset, int latestWriteOffset, int mappedSize) throws IOException {
        this.topic = topicName;
        this.queueId = queueId;
        this.wrotePosition = latestWriteOffset;
        String filePath = getLatestConsumeQueueFile();
        this.doMMap(filePath, startOffset, latestWriteOffset, mappedSize);
        //默认非公平
        putMessageLock = new UnFailReentrantLock();
    }

    /**
     * 执行mmap步骤
     *
     * @param filePath
     * @param startOffset
     * @param mappedSize
     * @throws IOException
     */
    private void doMMap(String filePath, int startOffset, int latestWriteOffset, int mappedSize) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filePath is " + filePath + " inValid");
        }
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mappedSize);
        this.readBuffer = mappedByteBuffer.slice();
        this.mappedByteBuffer.position(latestWriteOffset);
    }


    public void writeContent(byte[] content, boolean force) {
        try {
            putMessageLock.lock();
            mappedByteBuffer.put(content);
            // 更新写入位置
            wrotePosition = mappedByteBuffer.position();
            if (force) {
                mappedByteBuffer.force();
            }
        } finally {
            putMessageLock.unLock();
        }
    }

    public void writeContent(byte[] content) {
        writeContent(content, false);
    }


    /**
     * 读取consumequeue数据内容
     *
     * @param pos      消息读取开始位置
     * @param msgCount 消息条数
     * @return
     */
    public List<byte[]> readContent(int pos, int msgCount) {
        // 添加边界检查
        if (pos < 0 || msgCount <= 0 || pos >= wrotePosition) {
            return new ArrayList<>();
        }


        ByteBuffer readBuf = readBuffer.slice();

        // 限制读取范围不超过实际写入位置
        int availableMessages = (wrotePosition - pos) / BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE;
        int actualMsgCount = Math.min(msgCount, availableMessages);

        readBuf.position(pos);

        List<byte[]> loadContentList = new ArrayList<>();
        for (int i = 0; i < actualMsgCount; i++) {
            byte[] content = new byte[BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE];
            readBuf.get(content);
            loadContentList.add(content);
        }
        return loadContentList;
    }

    public byte[] readContentAll(int pos, int msgCount) {
        ByteBuffer readBuf = readBuffer.slice();
        int allSize = msgCount * BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE;
        byte[] bytes = new byte[allSize];
        readBuf.get(bytes);
        return bytes;
    }

    /**
     * 获取最新的commitLog文件路径
     *
     * @return
     */
    private String getLatestConsumeQueueFile() {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new IllegalArgumentException("topic is inValid! topicName is " + topic);
        }
        List<QueueModel> queueModelList = eagleMqTopicModel.getQueueList();
        QueueModel queueModel = queueModelList.get(queueId);
        if (queueModel == null) {
            throw new IllegalArgumentException("queueId is inValid! queueId is " + queueId);
        }
        int remainCapacity = queueModel.remainCapacity();
        String filePath = null;
        if (remainCapacity <= 0) {
            //已经写满了
            filePath = this.createNewConsumeQueueFile(queueModel.getFileName());
        } else {
            //还有机会写入
            filePath = LogFileNameUtil.buildConsumeQueueFilePath(topic, queueId, queueModel.getFileName());
        }
        return filePath;
    }

    private String createNewConsumeQueueFile(String fileName) {
        String newFileName = LogFileNameUtil.incrConsumeQueueFileName(fileName);
        String newFilePath = LogFileNameUtil.buildConsumeQueueFilePath(topic, queueId, newFileName);
        File newConsumeQueueFile = new File(newFilePath);
        try {
            //新的commitLog文件创建
            boolean flag = newConsumeQueueFile.createNewFile();
            if (!flag) {
                throw new RuntimeException("创建consume queue文件失败 ， 文件地址：" + newFilePath);
            }
            log.info("创建了新的consumeQueue文件");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return newFilePath;
    }

}
