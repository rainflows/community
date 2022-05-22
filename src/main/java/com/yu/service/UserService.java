package com.yu.service;

import com.yu.dao.LoginTicketMapper;
import com.yu.dao.UserMapper;
import com.yu.pojo.LoginTicket;
import com.yu.pojo.User;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import com.yu.utils.MailClient;
import com.yu.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 *
 * @author yu
 * @date 2022/05/09
 */
@Service
public class UserService implements CommunityConstant {
    /**
     * 用户映射器
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 登录凭证映射器
     */
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    /**
     * 邮件客户端
     */
    @Autowired
    private MailClient mailClient;

    /**
     * 模板引擎
     */
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 域
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 上下文路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 找到用户id
     *
     * @param id id
     * @return {@link User}
     */
    public User findUserById(int id) {
//        return userMapper.selectById(id);

        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * 注册
     *
     * @param user 用户
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> register(User user) {
        HashMap<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        // 验证账号，邮箱
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1 != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        user1 = userMapper.selectByEmail(user.getEmail());
        if (user1 != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.MD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "账号激活", content);
        return map;
    }

    /**
     * 激活
     *
     * @param userId 用户id
     * @param code   代码
     * @return int
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 优化2:使用Redis存储登录凭证
     */

    /**
     * 登录
     *
     * @param username      用户名
     * @param password      密码
     * @param expiredSecond 过期秒数
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> login(String username, String password, long expiredSecond) {
        HashMap<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能让为空");
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能让为空");
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }
        // 验证密码
        password = CommunityUtil.MD5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("password", "密码不正确");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSecond * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 注销
     *
     * @param ticket 凭证
     */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);

        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 找到登录凭证
     *
     * @param ticket 凭证
     * @return {@link LoginTicket}
     */
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);

        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * 更新头像
     *
     * @param userId    用户id
     * @param headerUrl 头url
     * @return int
     */
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * 验证电子邮件
     *
     * @param email 电子邮件
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> verifyEmail(String email) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(email)) {
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return map;
        } else {
            // 如果能查到这个邮箱就发送邮件
            Context context = new Context();
            context.setVariable("email", email);
            String code = CommunityUtil.generateUUID().substring(0, 4);
            context.setVariable("verifyCode", code);
            String content = templateEngine.process("/mail/forget", context);
            mailClient.sendMail(email, "找回密码", content);
            map.put("code", code);
        }
        map.put("user", user);
        return map;
    }

    /**
     * 重置密码
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> resetPassword(String email, String password) {
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册");
            return map;
        }
        password = CommunityUtil.MD5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        map.put("user", user);
        return map;
    }

    /**
     * 更新密码
     *
     * @param userId      用户id
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }
        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.MD5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }
        // 更新密码
        newPassword = CommunityUtil.MD5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }

    /**
     * 找到用户名字
     *
     * @param username 用户名
     * @return {@link User}
     */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 优化3:使用Redis缓存用户信息
     */

    /**
     * 1.优先从缓存中取值
     *
     * @param userId 用户id
     * @return {@link User}
     */
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * 2.若是取不到初始化缓存数据
     *
     * @param userId 用户id
     * @return {@link User}
     */
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 3.数据变更时清除缓存数据
     *
     * @param userId 用户id
     */
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
