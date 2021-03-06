package com.yu.controller;

import com.yu.event.EventProducer;
import com.yu.pojo.*;
import com.yu.service.CommentService;
import com.yu.service.DiscussPostService;
import com.yu.service.LikeService;
import com.yu.service.UserService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import com.yu.utils.HostHolder;
import com.yu.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 讨论帖控制器
 *
 * @author yu
 * @date 2022/05/16
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    /**
     * 讨论帖服务
     */
    @Autowired
    private DiscussPostService discussPostService;

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
     * 评论服务
     */
    @Autowired
    private CommentService commentService;

    /**
     * 点赞服务
     */
    @Autowired
    private LikeService likeService;

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
     * 添加讨论帖
     *
     * @param title   标题
     * @param content 内容
     * @return {@link String}
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "尚未登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 得到讨论帖
     *
     * @param model         模型
     * @param discussPostId 讨论帖子id
     * @return {@link String}
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int discussPostId, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST,
                post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        HashMap<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    /**
     * 置顶
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        // 触发发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        // 触发发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        eventProducer.fireEvent(event);
        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 设置删除
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        // 触发删帖事件
        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
