package com.yu.dao;

import com.yu.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评论映射器
 *
 * @author yu
 * @date 2022/05/16
 */
@Mapper
public interface CommentMapper {
    /**
     * 根据实体查询评论
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @param offset     抵消
     * @param limit      限制
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 根据实体查询数量
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return int
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 插入评论
     *
     * @param comment 评论
     * @return int
     */
    int insertComment(Comment comment);

    /**
     * 根据id查询评论
     *
     * @param id id
     * @return {@link Comment}
     */
    Comment selectCommentById(int id);

    /**
     * 根据用户查询评论
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    /**
     * 根据用户查询数量
     *
     * @param userId 用户id
     * @return int
     */
    int selectCountByUser(int userId);
}
