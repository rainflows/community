package com.yu.controller;

import com.yu.annotation.LoginRequired;
import com.yu.pojo.User;
import com.yu.service.UserService;
import com.yu.utils.CommunityUtil;
import com.yu.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * 用户控制器
 *
 * @author yu
 * @date 2022/05/12
 */
@Controller
@RequestMapping("/user")
public class UserController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 上传路径
     */
    @Value("${community.path.upload}")
    private String uploadPath;

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
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 主机架
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 得到设置页面
     *
     * @return {@link String}
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 上传头像
     *
     * @param multipartFile 多部分文件
     * @param model         模型
     * @return {@link String}
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile multipartFile, Model model) {
        if (multipartFile == null) {
            model.addAttribute("error", "您还没有上传图片");
            return "/site/setting";
        }
        String filename = multipartFile.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确");
            return "/site/setting";
        }
        /**
         * 生成随机文件名
         */
        filename = CommunityUtil.generateUUID() + suffix;
        /**
         * 确定文件存放路径
         */
        File dest = new File(uploadPath + "/" + filename);
        try {
            multipartFile.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        /**
         * 更新当前文件路径
         */
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headUrl);
        return "redirect:/index";
    }

    /**
     * 获取头像
     *
     * @param filename 文件名
     * @param response 响应
     */
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        /**
         * 服务器存放路径
         */
        filename = uploadPath + "/" + filename;
        /**
         * 文件后缀
         */
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        /**
         * 响应图片
         */
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filename)){
            OutputStream stream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                stream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    /**
     * 更新密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param model       模型
     * @return {@link String}
     */
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }
}
