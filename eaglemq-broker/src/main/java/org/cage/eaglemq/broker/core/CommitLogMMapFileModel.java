package org.cage.eaglemq.broker.core;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.model.*;
import org.cage.eaglemq.broker.strategy.SendMessageStrategy;
import org.cage.eaglemq.broker.utils.LogFileNameUtil;
import org.cage.eaglemq.broker.utils.PutMessageLock;
import org.cage.eaglemq.broker.utils.UnFailReentrantLock;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.dto.ConsumeMsgCommitLogDTO;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author idea
 * @Date: Created in 22:50 2024/3/25
 * @Description 最基础的mmap对象模型
 */
@Slf4j
public class CommitLogMMapFileModel {

    private String topic;

    private File file;

    private MappedByteBuffer mappedByteBuffer;

    private ByteBuffer readByteBuffer;


    private PutMessageLock putMessageLock;

    private SendMessageStrategy sendMessageStrategy;


    // topic 主题预备映射： 映射那个主题 那个队列 从哪里映射到哪里

    /**
     * 指定offset做文件的映射
     *
     * @param topicName   消息主题
     * @param startOffset 开始映射的offset
     * @param mappedSize  映射的体积 (byte)
     */
    public void loadFileInMMap(String topicName, int startOffset, int mappedSize, SendMessageStrategy strategy) throws IOException {
        this.sendMessageStrategy = strategy;
        this.topic = topicName;
        String filePath = getLatestCommitLogFile(topicName);
        this.doMMap(filePath, startOffset, mappedSize);
        //默认非公平
        putMessageLock = new UnFailReentrantLock();
    }

    /**
     * 做commit log 指定文件的预映射
     *
     * @param filePath
     * @param startOffset
     * @param mappedSize
     * @throws IOException
     */
    private void doMMap(String filePath, int startOffset, int mappedSize) throws IOException {
        this.file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("filePath is " + filePath + " inValid");
        }
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        this.mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, startOffset, mappedSize);
        //从指定位置开始追加写入
        this.readByteBuffer = mappedByteBuffer.slice();
        this.mappedByteBuffer.position(eagleMqTopicModel.getCommitLogModel().getOffset().get());
    }

    /**
     * 写入数据到磁盘当中
     *
     * @param messageDTO
     */
    public void writeContent(MessageDTO messageDTO, boolean force) throws IOException {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new IllegalArgumentException("eagleMqTopicModel is null");
        }
        CommitLogModel commitLogModel = eagleMqTopicModel.getCommitLogModel();
        if (commitLogModel == null) {
            throw new IllegalArgumentException("commitLogModel is null");
        }

        // 加锁 保障commit log 的顺序性 以及全部消息中间件的有序性
        putMessageLock.lock();
        CommitLogMessageModel commitLogMessageModel = new CommitLogMessageModel();
        commitLogMessageModel.setContent(messageDTO.getBody());
        this.checkCommitLogHasEnableSpace(commitLogMessageModel);
        byte[] writeContent = commitLogMessageModel.getContent();
        mappedByteBuffer.put(writeContent);
        log.info("写入到  {}  这个commit log文件的messageDTO对象：{}, messageBody内容：{}", this.file.getName(), messageDTO, new String(writeContent));
        AtomicInteger currentLatestMsgOffset = commitLogModel.getOffset();
        this.dispatcher(messageDTO, currentLatestMsgOffset.get());
        currentLatestMsgOffset.addAndGet(writeContent.length);
        if (force) {
            //强制刷盘
            mappedByteBuffer.force();
        }
        putMessageLock.unLock();
    }

    /**
     * 把commit log 文件 派发到consume queue文件中
     *
     * @param messageDTO
     * @param commitLogMessageStartIndex
     */
    private void dispatcher(MessageDTO messageDTO, int commitLogMessageStartIndex) {
        // 判断topic 的合法性
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new RuntimeException("topic is undefined");
        }

        // 计算 queueId
        int queueId;
        if (messageDTO.getQueueId() >= 0) {
            queueId = messageDTO.getQueueId();
        } else {
            queueId = sendMessageStrategy.getQueueId();
        }

        // 构建queue 消息对象
        ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
        consumeQueueDetailModel.setCommitLogFileName(Integer.parseInt(eagleMqTopicModel.getCommitLogModel().getFileName()));
        consumeQueueDetailModel.setMessageIndex(commitLogMessageStartIndex);
        consumeQueueDetailModel.setRetryTime(messageDTO.getCurrentRetryTime());
        consumeQueueDetailModel.setMessageLength(messageDTO.getBody().length);
        byte[] content = consumeQueueDetailModel.convertToBytes();
        log.info("写入队列数据：{}", JSON.toJSONString(consumeQueueDetailModel));

        // 把消息写入到consume queue 中
        List<ConsumeQueueMMapFileModel> queueMMapFileModelList = CommonCache.getConsumeQueueMMapFileModelManager().get(messageDTO.getTopic());
        ConsumeQueueMMapFileModel consumeQueueMMapFileModel = queueMMapFileModelList.stream().filter(item -> item.getQueueId().equals(messageDTO.getQueueId())).findFirst().orElse(null);
        if (consumeQueueMMapFileModel == null) {
            throw new IllegalArgumentException("queueId 非法");
        }
        consumeQueueMMapFileModel.writeContent(content);

        // 刷新 queue 的偏移量
        QueueModel queueModel = eagleMqTopicModel.getQueueList().get(queueId);
        queueModel.getLatestOffset().addAndGet(content.length);

    }

    /**
     * 更高性能的一种写入api
     *
     * @param messageDTO
     */
    public void writeContent(MessageDTO messageDTO) throws IOException {
        this.writeContent(messageDTO, false);

    }

    public ConsumeMsgCommitLogDTO readContent(int pos, int length) {
        ByteBuffer readBuf = readByteBuffer.slice();
        readBuf.position(pos);
        byte[] readBytes = new byte[length];
        readBuf.get(readBytes);
        ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = new ConsumeMsgCommitLogDTO();
        consumeMsgCommitLogDTO.setBody(readBytes);
        consumeMsgCommitLogDTO.setFileName(file.getName());
        consumeMsgCommitLogDTO.setCommitLogOffset(pos);
        consumeMsgCommitLogDTO.setCommitLogSize(length);
        return consumeMsgCommitLogDTO;
    }


    /**
     * 释放mmap内存占用
     */
    public void clean() {
        if (mappedByteBuffer == null || !mappedByteBuffer.isDirect() || mappedByteBuffer.capacity() == 0)
            return;
        invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
    }

    private void checkCommitLogHasEnableSpace(CommitLogMessageModel commitLogMessageModel) throws IOException {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(this.topic);
        CommitLogModel commitLogModel = eagleMqTopicModel.getCommitLogModel();
        long writeAbleOffsetNum = commitLogModel.remainCapacity();
        //空间不足，需要创建新的commitLog文件并且做映射
        if (writeAbleOffsetNum < commitLogMessageModel.getContent().length) {
            //00000000文件 -》00000001文件
            //commitLog剩余150byte大小的空间，最新的消息体积是151byte
            CommitLogFilePath commitLogFilePath = this.createNewCommitLogFile(topic, commitLogModel);
            commitLogModel.setOffsetLimit(BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
            commitLogModel.setOffset(new AtomicInteger(0));
            commitLogModel.setFileName(commitLogFilePath.getFileName());
            //新文件路径映射进来
            this.doMMap(commitLogFilePath.getFilePath(), 0, BrokerConstants.COMMIT_LOG_DEFAULT_MMAP_SIZE);
        }
    }


    private Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    private ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";
        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null) {
            return buffer;
        } else {
            return viewed(viewedBuffer);
        }
    }


    /**
     * 获取最新的commitLog文件路径
     *
     * @param topicName
     * @return
     */
    private String getLatestCommitLogFile(String topicName) {
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topicName);
        if (eagleMqTopicModel == null) {
            throw new IllegalArgumentException("topic is inValid! topicName is " + topicName);
        }

        CommitLogModel commitLogModel = eagleMqTopicModel.getCommitLogModel();
        int remainCapacity = commitLogModel.remainCapacity();
        String filePath = null;
        if (remainCapacity <= 0) {
            // 创建文件
            CommitLogFilePath commitLogFilePath = this.createNewCommitLogFile(topicName, commitLogModel);
            filePath = commitLogFilePath.getFilePath();
        } else {
            filePath = LogFileNameUtil.buildCommitLogFilePath(topicName, commitLogModel.getFileName());

        }
        return filePath;
    }

    /**
     * 创建最新的commit log 文件
     *
     * @param topicName
     * @param commitLogModel
     * @return
     */
    private CommitLogFilePath createNewCommitLogFile(String topicName, CommitLogModel commitLogModel) {
        String newFileName = LogFileNameUtil.incrCommitLogFileName(commitLogModel.getFileName());
        String newFilePath = LogFileNameUtil.buildCommitLogFilePath(topicName, newFileName);
        File newCommitLogFile = new File(newFilePath);
        try {
            //新的commitLog文件创建
            boolean newFile = newCommitLogFile.createNewFile();
            if (!newFile) {
                throw new RuntimeException("创建新的commit log 文件失败, 文件path：" + newFilePath);
            }
            log.info("创建了新的commitLog文件");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CommitLogFilePath(newFileName, newFilePath);
    }


    @Data
    @AllArgsConstructor
    class CommitLogFilePath {
        private String fileName;
        private String filePath;
    }

}
