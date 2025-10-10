package org.cage.eaglemq.broker.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import org.cage.eaglemq.broker.model.CommitLogModel;
import org.cage.eaglemq.broker.utils.PutMessageLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author idea
 * @Date: Created in 22:50 2024/3/25
 * @Description 最基础的mmap对象模型
 */
@Slf4j
public class CommitLogMMapFileModel {

    private String topic;
    private Integer queueId;
    private MappedByteBuffer mappedByteBuffer;
    private PutMessageLock putMessageLock;


    public void loadFileInMMap(String topicName, Integer queueId) {

    }
    /**
     * 释放mmap内存占用
     */
    public void clean() {
        if (mappedByteBuffer == null || !mappedByteBuffer.isDirect() || mappedByteBuffer.capacity() == 0)
            return;
        invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
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
        if (viewedBuffer == null)
            return buffer;
        else
            return viewed(viewedBuffer);
    }


    class CommitLogFilePath {
        private String fileName;
        private String filePath;

        public CommitLogFilePath(String fileName, String filePath) {
            this.fileName = fileName;
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

//    private CommitLogFilePath createNewCommitLogFile(String topicName, CommitLogModel commitLogModel) {
//        String newFileName = LogFileNameUtil.incrCommitLogFileName(commitLogModel.getFileName());
//        String newFilePath = LogFileNameUtil.buildCommitLogFilePath(topicName, newFileName);
//        File newCommitLogFile = new File(newFilePath);
//        try {
//            //新的commitLog文件创建
//            newCommitLogFile.createNewFile();
//            logger.info("创建了新的commitLog文件");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return new CommitLogFilePath(newFileName, newFilePath);
//    }
}
