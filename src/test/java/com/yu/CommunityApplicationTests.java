package com.yu;

import com.yu.dao.DiscussPostMapper;
import com.yu.dao.LoginTicketMapper;
import com.yu.dao.MessageMapper;
import com.yu.dao.UserMapper;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.LoginTicket;
import com.yu.pojo.Message;
import com.yu.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class CommunityApplicationTests {
    /**
     * 用户映射器
     */
    @Autowired
    private UserMapper userMapper;
    /**
     * 讨论后映射器
     */
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 登录票映射器
     */
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    /**
     * 消息映射器
     */
    @Autowired
    private MessageMapper messageMapper;

    @Test
    void contextLoads() {
    }

    @Test
    public void testUser() {
        System.out.println(userMapper.selectById(101));

        System.out.println(userMapper.selectByName("liubei"));

        System.out.println(userMapper.selectByEmail("nowcoder101@sina.com"));

//		User user = new User();
//		user.setUsername("test");
//		user.setPassword("666666");
//		user.setSalt("abc");
//		user.setEmail("test@qq.com");
//		user.setHeaderUrl("http://www.nowcoder.com/101.png");
//		user.setCreateTime(new Date());
//		System.out.println(userMapper.insertUser(user));
//		System.out.println(user.getId());
//
//		System.out.println(userMapper.updateHeader(150, "http://www.nowcoder.com/150.png"));
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 100);
        for (DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }
//		int rows = discussPostMapper.selectDiscussPosRows(149);
//		System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void selectLoginTicket() {
        LoginTicket ticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(ticket);

        loginTicketMapper.updateStatus("abc", 1);
        ticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(ticket);
    }

    @Test
    public void testMessageMapper() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }
        System.out.println();

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
        System.out.println();

        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages) {
            System.out.println(message);
        }
        System.out.println();

        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);
        System.out.println();

        int unreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(unreadCount);
    }
}
