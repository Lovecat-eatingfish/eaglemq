package com.cage.eaglemq.broker;

import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.core.CommitLogAppendHandler;
import org.cage.eaglemq.broker.core.ConsumeQueueAppendHandler;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName: SendMessage
 * PackageName: com.cage.eaglemq.broker
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午3:55
 * @Version: 1.0
 */
public class SendMessage {
    public static void main(String[] args) throws IOException {
        simpleSendMessage();


    }

    private static void simpleSendMessage() throws IOException {
        CommitLogAppendHandler commitLogAppendHandler = CommonCache.getCommitLogAppendHandler();

        String topic = "created_order_topic";
        for (int i = 0; i < 10; i++) {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setProducerId("producerA");
            messageDTO.setTopic(topic);
            messageDTO.setQueueId(0);
            messageDTO.setBody(("你好" + i).getBytes());
            messageDTO.setMessageId(UUID.randomUUID().toString());
            commitLogAppendHandler.appendMessageToCommitLog(messageDTO);
        }
    }

    public void testConcurrentSendMessage() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        String topic = "created_order_topic";

        CommitLogAppendHandler commitLogAppendHandler = CommonCache.getCommitLogAppendHandler();
        CountDownLatch startLatch = new CountDownLatch(10);  // 线程准备就绪计数器
        CountDownLatch readyLatch = new CountDownLatch(1);   // 主线程控制开始信号
        CountDownLatch finishLatch = new CountDownLatch(10); // 线程完成计数器
        // 1 >  10000
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                startLatch.countDown();
                try {
                    readyLatch.await();
                    for (int j = 0;j < 100; j++) {
                        MessageDTO messageDTO = new MessageDTO();
                        messageDTO.setProducerId("producerA");
                        messageDTO.setTopic(topic);
                        messageDTO.setQueueId(0);
                        messageDTO.setBody(("你好1111").getBytes());
                        messageDTO.setMessageId(UUID.randomUUID().toString());
                        commitLogAppendHandler.appendMessageToCommitLog(messageDTO);
                        count.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }finally {
                    finishLatch.countDown();
                }
            }).start();

        }

        startLatch.await();
        readyLatch.countDown();
        finishLatch.await();
        System.out.println("总共的消息数量：" + count.get());
        TimeUnit.MINUTES.sleep(1);
    }
}
