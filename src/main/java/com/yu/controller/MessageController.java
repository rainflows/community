package com.yu.controller;

import com.alibaba.fastjson.JSONObject;
import com.yu.pojo.Message;
import com.yu.pojo.Page;
import com.yu.pojo.User;
import com.yu.service.MessageService;
import com.yu.service.UserService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import com.yu.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * 消息控制器
 *
 * @author yu
 * @date 2022/05/17
 */
@Controller
public class MessageController implements CommunityConstant {

    /**
     * 消息服务
     */
    @Autowired
    private MessageService messageService;

    /**
     * 用户持有者
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 得到私信列表
     *
     * @param model 模型
     * @param page  页面
     * @return {@link String}
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    /**
     * 得到详细信
     *
     * @param conversationId 会话id
     * @param page           页面
     * @param model          模型
     * @return {@link String}
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));

                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }

    /**
     * 得到私信目标
     *
     * @param conversationId 会话id
     * @return {@link User}
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    /**
     * 得到信id
     *
     * @param letterList 字母列表
     * @return {@link List}<{@link Integer}>
     */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 发私信
     *
     * @param toName  名字
     * @param content 内容
     * @return {@link String}
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除私信
     *
     * @param id id
     * @return {@link String}
     */
    @RequestMapping(path = "/letter/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteLetter(int id) {
        messageService.deleteMessage(id);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 得到通知列表
     *
     * @param model 模型
     * @return {@link String}
     */
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        // 查询评论类通知
        Message commentMessage = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (commentMessage != null) {
            Map<String, Object> commentMessageVO = new HashMap<>();
            commentMessageVO.put("message", commentMessage);

            String content = HtmlUtils.htmlUnescape(commentMessage.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            commentMessageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            commentMessageVO.put("entityType", data.get("entityType"));
            commentMessageVO.put("entityId", data.get("entityId"));
            commentMessageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            commentMessageVO.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            commentMessageVO.put("unreadCount", unreadCount);

            model.addAttribute("commentNotice", commentMessageVO);
        }

        // 查询点赞类通知
        Message likeMessage = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (likeMessage != null) {
            Map<String, Object> likeMessageVO = new HashMap<>();
            likeMessageVO.put("message", likeMessage);

            String content = HtmlUtils.htmlUnescape(likeMessage.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            likeMessageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            likeMessageVO.put("entityType", data.get("entityType"));
            likeMessageVO.put("entityId", data.get("entityId"));
            likeMessageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            likeMessageVO.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            likeMessageVO.put("unreadCount", unreadCount);

            model.addAttribute("likeNotice", likeMessageVO);
        }

        // 查询关注类通知
        Message followMessage = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (followMessage != null) {
            Map<String, Object> followMessageVO = new HashMap<>();
            followMessageVO.put("message", followMessage);

            String content = HtmlUtils.htmlUnescape(followMessage.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            followMessageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            followMessageVO.put("entityType", data.get("entityType"));
            followMessageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            followMessageVO.put("count", count);

            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            followMessageVO.put("unreadCount", unreadCount);

            model.addAttribute("followNotice", followMessageVO);
        }

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    /**
     * 得到通知内容
     *
     * @param topic 主题
     * @param page  页面
     * @param model 模型
     * @return {@link String}
     */
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticesList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticesList != null) {
            for (Message notice : noticesList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticesList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
