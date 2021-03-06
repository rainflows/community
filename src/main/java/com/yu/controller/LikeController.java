package com.yu.controller;

import com.yu.event.EventProducer;
import com.yu.pojo.Event;
import com.yu.pojo.User;
import com.yu.service.LikeService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import com.yu.utils.HostHolder;
import com.yu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞控制器
 *
 * @author yu
 * @date 2022/05/19
 */
@Controller
public class LikeController implements CommunityConstant {

    /**
     * 点赞服务
     */
    @Autowired
    private LikeService likeService;

    /**
     * 用户持有者
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 事件生产者
     */
    @Autowired
    private EventProducer eventProducer;

    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     *
     * @param entityType   实体类型
     * @param entityId     实体id
     * @param entityUserId 实体用户id
     * @param postId       讨论帖id
     * @return {@link String}
     */
    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE);
            event.setUserId(hostHolder.getUser().getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            event.setData("postId", postId);

            eventProducer.fireEvent(event);
        }
        // 计算帖子分数
        if (entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
