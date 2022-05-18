package com.yu.service;

import com.yu.dao.DiscussPostMapper;
import com.yu.pojo.DiscussPost;
import com.yu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 讨论后服务
 *
 * @author yu
 * @date 2022/05/09
 */
@Service
public class DiscussPostService {
    /**
     * 讨论后映射器
     */
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 敏感过滤器
     */
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 找到讨论帖子
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link DiscussPost}>
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 找到讨论帖子行
     *
     * @param userId 用户id
     * @return int
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPosRows(userId);
    }

    /**
     * 添加讨论后
     *
     * @param discussPost 讨论后
     * @return int
     */
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 找到讨论帖子id
     *
     * @param id id
     * @return {@link DiscussPost}
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新评论数
     *
     * @param id           id
     * @param commentCount 评论数
     * @return int
     */
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }
}
