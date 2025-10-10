package org.cage.eaglemq.broker.core;

import lombok.extern.slf4j.Slf4j;
import org.cage.eaglemq.broker.cache.CommonCache;
import org.cage.eaglemq.broker.model.*;
import org.cage.eaglemq.common.constants.BrokerConstants;
import org.cage.eaglemq.common.dto.ConsumeMsgCommitLogDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ConsumeQueueConsumeHandler
 * PackageName: org.cage.eaglemq.broker.core
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/10 下午4:13
 * @Version: 1.0
 */
@Slf4j
public class ConsumeQueueConsumeHandler {

    /**
     * 读取当前最新N条consumeQueue的消息内容,并且返回commitLog原始数据
     *
     * @return
     */
    public List<ConsumeMsgCommitLogDTO> consume(ConsumeQueueConsumeReqModel consumeQueueConsumeReqModel) {
        // 1: topic 校验
        String topic = consumeQueueConsumeReqModel.getTopic();
        EagleMqTopicModel eagleMqTopicModel = CommonCache.getEagleMqTopicModelMap().get(topic);
        if (eagleMqTopicModel == null) {
            throw new RuntimeException("topic " + topic + " not exist!");
        }
        List<QueueModel> queueList = eagleMqTopicModel.getQueueList();

        String consumeGroup = consumeQueueConsumeReqModel.getConsumeGroup();
        int queueId = consumeQueueConsumeReqModel.getQueueId();
        int batchSize = consumeQueueConsumeReqModel.getBatchSize();

        // 2： 对 消费组的 OffsetTable机器内部变量的初始化
        ConsumeQueueOffsetModel.OffsetTable offsetTable = CommonCache.getConsumeQueueOffsetModel().getOffsetTable();
        Map<String, ConsumeQueueOffsetModel.ConsumerGroupDetail> topicConsumerGroupDetail = offsetTable.getTopicConsumerGroupDetail();
        ConsumeQueueOffsetModel.ConsumerGroupDetail consumerGroupDetail = topicConsumerGroupDetail.get(topic);
        if (consumerGroupDetail == null) {
            consumerGroupDetail = new ConsumeQueueOffsetModel.ConsumerGroupDetail();
            topicConsumerGroupDetail.put(topic, consumerGroupDetail);
        }
        Map<String, Map<String, String>> consumerGroupDetailMap = consumerGroupDetail.getConsumerGroupDetailMap();

        Map<String, String> consumerOffsetDetailsMap = consumerGroupDetailMap.get(consumeGroup);
        if (consumerOffsetDetailsMap == null) {
            consumerOffsetDetailsMap = new HashMap<>();
            for (QueueModel queueModel : queueList) {
                consumerOffsetDetailsMap.put(String.valueOf(queueModel.getId()), queueModel.getFileName() + "#" + queueModel.getLatestOffset().get());
            }
            consumerGroupDetailMap.put(consumeGroup, consumerOffsetDetailsMap);
        }
        String offsetStrInfo = consumerOffsetDetailsMap.get(String.valueOf(queueId));
        String[] offsetStr = offsetStrInfo.split("#");
        int consumeQueueOffset = Integer.parseInt(offsetStr[1]);
        QueueModel queueModel = queueList.get(queueId);
        // 消费组已经消费到最新的 queue 的内容了 直接return null
        if (queueModel.getLatestOffset().get() <= consumeQueueOffset) {
            return null;
        }

        // 先从consume queue 文件中读取  16 个字节的数据： 那个commit log 文件、起始索引、body长度、重试次数
        List<ConsumeQueueMMapFileModel> consumeQueueMMapFileModels = CommonCache.getConsumeQueueMMapFileModelManager().get(topic);
        ConsumeQueueMMapFileModel consumeQueueMMapFileModel = consumeQueueMMapFileModels.get(queueId);


        List<ConsumeMsgCommitLogDTO> commitLogBodyContentList = new ArrayList<>();
        readFromCommitLogI(topic, consumeQueueMMapFileModel, consumeQueueOffset, batchSize, commitLogBodyContentList);
//        byte[] bytes = consumeQueueMMapFileModel.readContentAll(consumeQueueOffset, batchSize);
//        int i = 0;
//        byte[] contentByte = new byte[BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE];
//        int k = 0;
//        while (i < bytes.length) {
//            if ((i + 1) % BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE == 0) {
//                ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
//                consumeQueueDetailModel.buildFromBytes(contentByte);
//
//                CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(topic);
//                ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(consumeQueueDetailModel.getMessageIndex(), consumeQueueDetailModel.getMessageLength());
//                commitLogBodyContentList.add(consumeMsgCommitLogDTO);
//                k = 0;
//            }
//            contentByte[k++] = bytes[i];
//            i++;
//        }


//        List<byte[]> consumeQueueContentList = consumeQueueMMapFileModel.readContent(consumeQueueOffset, batchSize);
//
//        for (byte[] content : consumeQueueContentList) {
//            // 从 consume queue 中读取数据
//            ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
//            consumeQueueDetailModel.buildFromBytes(content);
//
//            // 在用consume queue 中的索引数据读取commit log 文件的数据
//            CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(topic);
//            ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(consumeQueueDetailModel.getMessageIndex(), consumeQueueDetailModel.getMessageLength());
//            commitLogBodyContentList.add(consumeMsgCommitLogDTO);
//        }

        return commitLogBodyContentList;
    }

    // 读取方式一：
    private void readFromCommitLogI(String topic, ConsumeQueueMMapFileModel consumeQueueMMapFileModel, int consumeQueueOffset, int batchSize, List<ConsumeMsgCommitLogDTO> commitLogBodyContentList) {
        List<byte[]> consumeQueueContentList = consumeQueueMMapFileModel.readContent(consumeQueueOffset, batchSize);

        for (byte[] content : consumeQueueContentList) {
            // 从 consume queue 中读取数据
            ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
            consumeQueueDetailModel.buildFromBytes(content);

            // 在用consume queue 中的索引数据读取commit log 文件的数据
            CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(topic);
            ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(consumeQueueDetailModel.getMessageIndex(), consumeQueueDetailModel.getMessageLength());
            commitLogBodyContentList.add(consumeMsgCommitLogDTO);
        }
    }

    // 读取方式二：
    private void readFromCommitLogII(String topic, ConsumeQueueMMapFileModel consumeQueueMMapFileModel, int consumeQueueOffset, int batchSize, List<ConsumeMsgCommitLogDTO> commitLogBodyContentList) {
        byte[] bytes = consumeQueueMMapFileModel.readContentAll(consumeQueueOffset, batchSize);
        int i = 0;
        byte[] contentByte = new byte[BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE];
        int k = 0;
        while (i < bytes.length) {
            if ((i + 1) % BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE == 0) {
                ConsumeQueueDetailModel consumeQueueDetailModel = new ConsumeQueueDetailModel();
                consumeQueueDetailModel.buildFromBytes(contentByte);

                CommitLogMMapFileModel commitLogMMapFileModel = CommonCache.getCommitLogMMapFileModelManager().get(topic);
                ConsumeMsgCommitLogDTO consumeMsgCommitLogDTO = commitLogMMapFileModel.readContent(consumeQueueDetailModel.getMessageIndex(), consumeQueueDetailModel.getMessageLength());
                commitLogBodyContentList.add(consumeMsgCommitLogDTO);
                k = 0;
            }
            contentByte[k++] = bytes[i];
            i++;
        }
    }

    public boolean ack(String topic, String consumeGroup, Integer queueId) {
        try {
            ConsumeQueueOffsetModel.OffsetTable offsetTable = CommonCache.getConsumeQueueOffsetModel().getOffsetTable();
            Map<String, ConsumeQueueOffsetModel.ConsumerGroupDetail> topicConsumerGroupDetail = offsetTable.getTopicConsumerGroupDetail();
            ConsumeQueueOffsetModel.ConsumerGroupDetail consumerGroupDetail = topicConsumerGroupDetail.get(topic);
            Map<String, String> consumeQueueOffsetMap = consumerGroupDetail.getConsumerGroupDetailMap().get(consumeGroup);
            String offsetStrInfo = consumeQueueOffsetMap.get(String.valueOf(queueId));
            String[] offsetStrArr = offsetStrInfo.split("#");
            String fileName = offsetStrArr[0];
            int currentOffset = Integer.parseInt(offsetStrArr[1]);
            currentOffset += BrokerConstants.CONSUME_QUEUE_EACH_MSG_SIZE;
            consumeQueueOffsetMap.put(String.valueOf(queueId), fileName + "#" + currentOffset);
            return true;
        } catch (NumberFormatException e) {
            log.info("broker ack 失败：{}", e.getMessage());
            return false;
        }
    }
}
