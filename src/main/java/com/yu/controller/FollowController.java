package com.yu.controller;

import com.yu.event.EventProducer;
import com.yu.pojo.Event;
import com.yu.pojo.Page;
import com.yu.pojo.User;
import com.yu.service.FollowService;
import com.yu.service.UserService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import com.yu.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注控制器
 *
 * @author yu
 * @date 2022/05/19
 */
@Controller
public class FollowController implements CommunityConstant {

    /**
     * 关注服务
     */
    @Autowired
    private FollowService followService;

    /**
     * 用户持有者
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 事件生产者
     */
    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注
     *
     * @return {@link String}
     */
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        // 此处可以用拦截器检查，使登录后才能访问
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注");
    }

    /**
     * 取消关注
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return {@link String}
     */
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    /**
     * 得到某用户关注的人
     *
     * @param userId 用户id
     * @param page   页面
     * @param model  模型
     * @return {@link String}
     */
    @RequestMapping(path = "/followee/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followee/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User user1 = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user1.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "/site/followee";
    }

    /**
     * 得到用户的粉丝
     *
     * @return {@link String}
     */
    @RequestMapping(value = "/follower/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/follower/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User user1 = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user1.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    /**
     * 是否关注
     *
     * @param userId 用户id
     * @return boolean
     */
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
