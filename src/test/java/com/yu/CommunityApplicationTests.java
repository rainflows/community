package com.yu;

import com.yu.dao.DiscussPostMapper;
import com.yu.dao.UserMapper;
import com.yu.pojo.DiscussPost;
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

	@Test
	void contextLoads() {
	}

	@Test
	public void testUser(){
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
	public void testSelectPosts(){
		List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 100);
		for (DiscussPost discussPost : list) {
			System.out.println(discussPost);
		}
//		int rows = discussPostMapper.selectDiscussPosRows(149);
//		System.out.println(rows);
	}
}
