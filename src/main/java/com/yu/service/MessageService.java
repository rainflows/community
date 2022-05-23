package com.yu.service;

import com.yu.dao.MessageMapper;
import com.yu.pojo.Message;
import com.yu.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 消息服务
 *
 * @author yu
 * @date 2022/05/17
 */
@Service
public class MessageService {
    /**
     * 消息映射器
     */
    @Autowired
    private MessageMapper messageMapper;

    /**
     * 敏感过滤器
     */
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户会话列表
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link Message}>
     */
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询当前用户会话数量
     *
     * @param userId 用户id
     * @return int
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询每个会话所包含的私信列表
     *
     * @param conversationId 会话id
     * @param offset         抵消
     * @param limit          限制
     * @return {@link List}<{@link Message}>
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询每个会话所包含的私信数量
     *
     * @param conversationId 会话id
     * @return int
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信的数量
     *
     * @param userId         用户id
     * @param conversationId 会话id
     * @return int
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 添加消息
     *
     * @param message 消息
     * @return int
     */
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 读消息
     *
     * @param ids id
     * @return int
     */
    public int readMessage(List<Integer> ids) {

        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 删除消息
     *
     * @param id id
     * @return int
     */
    public int deleteMessage(int id) {

        return messageMapper.updateStatus(Arrays.asList(new Integer[]{id}), 2);
    }

    /**
     * 查询最新通知
     *
     * @param userId 用户id
     * @param topic  主题
     * @return {@link Message}
     */
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询通知数量
     *
     * @param userId 用户id
     * @param topic  主题
     * @return int
     */
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询未读通知数
     *
     * @param userId 用户id
     * @param topic  主题
     * @return int
     */
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * 查找通知列表
     *
     * @param userId 用户id
     * @param topic  主题
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link Message}>
     */
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotice(userId, topic, offset, limit);
    }
}
