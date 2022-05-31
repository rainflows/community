package com.yu.dao;

import com.yu.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 讨论帖映射器
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
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    /**
     * 选择讨论帖行数
     *
     * @param userId 用户id
     * @return int
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 插入讨论帖
     *
     * @param discussPost 讨论帖
     * @return int
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 选择讨论帖id
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

    /**
     * 更新类型
     *
     * @param id   id
     * @param type 类型
     * @return int
     */
    int updateType(int id, int type);

    /**
     * 更新状态
     *
     * @param id     id
     * @param status 状态
     * @return int
     */
    int updateStatus(int id, int status);

    /**
     * 更新分数
     *
     * @param id    id
     * @param score 分数
     * @return int
     */
    int updateScore(int id, double score);
}
