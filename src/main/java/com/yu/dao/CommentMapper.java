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
     * 选择实体发表评论
     *
     * @param entityType 实体类型
     * @param entityId   实体id
     * @param offset     抵消
     * @param limit      限制
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 选择计算实体
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
}
