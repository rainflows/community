package com.yu.service;

import com.yu.dao.DiscussPostMapper;
import com.yu.pojo.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * 找到讨论帖子
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link DiscussPost}>
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 找到讨论帖子行
     *
     * @param userId 用户id
     * @return int
     */
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPosRows(userId);
    }
}
