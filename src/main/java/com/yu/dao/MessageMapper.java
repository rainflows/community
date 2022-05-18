package com.yu.dao;

import com.yu.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息映射器
 *
 * @author yu
 * @date 2022/05/17
 */
@Mapper
public interface MessageMapper {
    /**
     * 查询当前用户的对话列表，针对每个会话值返回一条最新的消息
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link Message}>
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * 查询当前用户的对话数量
     *
     * @param userId 用户id
     * @return int
     */
    int selectConversationCount(int userId);

    /**
     * 查询每个会话包含的私信列表
     *
     * @param conversationId 会话id
     * @param offset         抵消
     * @param limit          限制
     * @return {@link List}<{@link Message}>
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询某个会话包含的私信数量
     *
     * @param conversationId 会话id
     * @return int
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询私信未读的数量
     *
     * @param userId         用户id
     * @param conversationId 会话id
     * @return int
     */
    int selectLetterUnreadCount(int userId, String conversationId);

    /**
     * 插入消息
     *
     * @param message 消息
     * @return int
     */
    int insertMessage(Message message);

    /**
     * 更新状态
     *
     * @param ids    id
     * @param status 状态
     * @return int
     */
    int updateStatus(List<Integer> ids, int status);

}
