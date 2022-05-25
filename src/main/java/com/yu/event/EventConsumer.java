package com.yu.event;

import com.alibaba.fastjson.JSONObject;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.Event;
import com.yu.pojo.Message;
import com.yu.service.DiscussPostService;
import com.yu.service.ElasticsearchService;
import com.yu.service.MessageService;
import com.yu.utils.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件消费者
 *
 * @author yu
 * @date 2022/05/23
 */
@Component
public class EventConsumer implements CommunityConstant {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    /**
     * 消息服务
     */
    @Autowired
    private MessageService messageService;

    /**
     * 讨论帖服务
     */
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * elasticsearch服务
     */
    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 处理评论消息
     *
     * @param record 记录
     */
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式有误！");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    /**
     * 处理发布消息
     *
     * @param record 记录
     */
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式有误！");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }
}
