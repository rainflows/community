package com.yu.controller;

import com.yu.event.EventProducer;
import com.yu.pojo.Comment;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.Event;
import com.yu.service.CommentService;
import com.yu.service.DiscussPostService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 评论控制器
 *
 * @author yu
 * @date 2022/05/17
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    /**
     * 评论服务
     */
    @Autowired
    private CommentService commentService;

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
     * 讨论帖服务
     */
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 添加评论
     *
     * @param discussPostId 讨论帖子id
     * @param comment       评论
     * @return {@link String}
     */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addCommentCount(comment);

        // 触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        event.setData("postId", discussPostId);
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
