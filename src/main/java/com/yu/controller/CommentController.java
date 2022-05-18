package com.yu.controller;

import com.yu.pojo.Comment;
import com.yu.service.CommentService;
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
public class CommentController {

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
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
