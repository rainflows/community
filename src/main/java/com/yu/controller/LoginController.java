package com.yu.controller;

import com.google.code.kaptcha.Producer;
import com.yu.pojo.User;
import com.yu.service.UserService;
import com.yu.utils.CommunityUtil;
import com.yu.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yu.utils.CommunityConstant.*;

/**
 * 登录控制器
 *
 * @author yu
 * @date 2022/05/10
 */
@Controller
public class LoginController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * kaptcha
     */
    @Autowired
    private Producer kaptchaProducer;

    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 上下文路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 获得注册页面
     *
     * @return {@link String}
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 注册
     *
     * @param model 模型
     * @param user  用户
     * @return {@link String}
     */
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，清尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 获得登录页面
     *
     * @return {@link String}
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 激活
     *
     * @param model  模型
     * @param userId 用户id
     * @param code   代码
     * @return {@link String}
     */
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int res = userService.activation(userId, code);
        if (res == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号现在可以正常使用");
            model.addAttribute("target", "/login");

        } else if (res == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已被激活过");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /**
     * 优化1:使用Redis存储验证码
     */

    /**
     * 得到kaptcha
     *
     * @param response 响应
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session(传的是text)
//        session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);


        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream stream = response.getOutputStream();
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }


    /**
     * 登录
     *
     * @param username     用户名
     * @param password     密码
     * @param code         代码
     * @param model        模型
     * @param response     响应
     * @param rememberMe   记得我
     * @param kaptchaOwner kaptcha所有者
     * @return {@link String}
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe, Model model,
                        HttpServletResponse response, HttpSession session, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");

        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }
        // 检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 注销
     *
     * @param ticket 凭证
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    /**
     * 获取忘记页面
     *
     * @return {@link String}
     */
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    /**
     * 获取验证码
     *
     * @param email   电子邮件
     * @param session 会话
     * @return {@link String}
     */
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getVerifyCode(String email, HttpSession session) {
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }
        // 发送邮件
        Map<String, Object> map = userService.verifyEmail(email);

        // 保存验证码
        if (map.containsKey("user")) {
            session.setAttribute("verifyCode", map.get("code"));
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1, "查询不到该邮箱注册信息");
        }
    }

    /**
     * 重置密码
     *
     * @param email      电子邮件
     * @param verifyCode 验证代码
     * @param password   密码
     * @param model      模型
     * @param session    会话
     * @return {@link String}
     */
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            return "redirect:/login";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}
