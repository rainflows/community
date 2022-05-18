package com.yu.dao;

import com.yu.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 讨论后映射器
 *
 * @author yu
 * @date 2022/05/09
 */
@Mapper
public interface DiscussPostMapper {
    /**
     * 选择讨论帖子
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link DiscussPost}>
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * 选择讨论pos行
     *
     * @param userId 用户id
     * @return int
     */
    int selectDiscussPosRows(@Param("userId") int userId);

    /**
     * 插入讨论后
     *
     * @param discussPost 讨论后
     * @return int
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 选择讨论postid
     *
     * @param id id
     * @return {@link DiscussPost}
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新评论数
     *
     * @param id           id
     * @param commentCount 评论数
     * @return int
     */
    int updateCommentCount(int id, int commentCount);
}
