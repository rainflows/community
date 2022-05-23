package com.yu.event;

import com.alibaba.fastjson.JSONObject;
import com.yu.pojo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件生产者
 *
 * @author yu
 * @date 2022/05/23
 */
@Component
public class EventProducer {

    /**
     * kafka模板
     */
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     *
     * @param event 事件
     */
    public void fireEvent(Event event){
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
