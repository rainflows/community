package com.yu.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.Event;
import com.yu.pojo.Message;
import com.yu.service.DiscussPostService;
import com.yu.service.ElasticsearchService;
import com.yu.service.MessageService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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
     * wk图像存储
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * wk图像命令
     */
    @Value("${wk.image.command}")
    private String wkImageCommand;

    /**
     * 访问密钥
     */
    @Value("${qiniu.key.access}")
    private String accessKey;

    /**
     * 秘密密钥
     */
    @Value("${qiniu.key.secret}")
    private String secretKey;

    /**
     * shareBucket name
     */
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    /**
     * 线程池任务调度器
     */
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

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

    /**
     * 处理删除消息
     *
     * @param record 记录
     */
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式有误！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    /**
     * 处理分享消息
     *
     * @param record 记录
     */
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式有误！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }

        // 启动定时器，监视该图片，若生成则上传七牛云
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {

        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private Long startTime;
        // 上传次数
        private int uploadTime;

        public void setFuture(Future future) {
            this.future = future;
        }

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            // 生成失效
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长，任务终止：" + fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTime >= 3) {
                logger.error("上传次数过多，任务终止：" + fileName);
                future.cancel(true);
                return;
            }
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s].", ++uploadTime, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //  指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Region.regionCnEast2()));
                try {
                    // 开始上传图片
                    Response response = manager.put(path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s].", uploadTime, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s]", uploadTime, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%d].", uploadTime, fileName));
                }
            } else {
                logger.info("等待图片生成[" + fileName + "]");
            }
        }
    }
}
