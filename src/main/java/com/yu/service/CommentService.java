package com.yu.service;

import com.yu.dao.CommentMapper;
import com.yu.pojo.Comment;
import com.yu.utils.CommunityConstant;
import com.yu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 评论服务
 *
 * @author yu
 * @date 2022/05/16
 */
@Service
public class CommentService implements CommunityConstant {
    /**
     * 评论映射器
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 敏感过滤器
     */
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 讨论后服务
     */
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 找到实体评论
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @param offset     抵消
     * @param limit      限制
     * @return {@link List}<{@link Comment}>
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 找到评论数
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return int
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论数
     *
     * @param comment 评论
     * @return int
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public int addCommentCount(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }
}
