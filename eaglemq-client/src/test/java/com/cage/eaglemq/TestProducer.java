package com.cage.eaglemq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.cage.eaglemq.client.producer.DefaultProducerImpl;
import org.cage.eaglemq.client.producer.SendResult;
import org.cage.eaglemq.common.dto.MessageDTO;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: TestProducer
 * PackageName: com.cage.eaglemq
 * Description:
 *
 * @Author: 32782
 * @Date: 2025/10/14 下午4:12
 * @Version: 1.0
 */
public class TestProducer {

    public static void main(String[] args) throws InterruptedException {
        DefaultProducerImpl producer = new DefaultProducerImpl();
        producer.setNameServerIp("127.0.0.1");
        producer.setNameServerPort(20000);
        producer.setNameServerPwd("eagle_mq");
        producer.setNameServerUser("eagle_mq");
        producer.setBrokerClusterGroup("eagle_mq_test_group");
        producer.start();


        for (int i = 0; i < 1; i++) {
            try {
                MessageDTO messageDTO = new MessageDTO();
                messageDTO.setTopic("user_enter");
                messageDTO.setSendWay(2);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", i);
                jsonObject.put("level", 1);
                jsonObject.put("enterTime", System.currentTimeMillis());
                messageDTO.setBody(jsonObject.toJSONString().getBytes());
                SendResult sendResult = producer.send(messageDTO);
                System.out.println(JSON.toJSONString(sendResult));
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
