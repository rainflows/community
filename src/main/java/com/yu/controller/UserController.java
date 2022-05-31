package com.yu.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.yu.annotation.LoginRequired;
import com.yu.pojo.Comment;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.Page;
import com.yu.pojo.User;
import com.yu.service.*;
import com.yu.utils.CommunityConstant;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户控制器
 *
 * @author yu
 * @date 2022/05/12
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

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
     * 用户持有者
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 点赞服务
     */
    @Autowired
    private LikeService likeService;

    /**
     * 关注服务
     */
    @Autowired
    private FollowService followService;

    /**
     * 讨论帖服务
     */
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 评论服务
     */
    @Autowired
    private CommentService commentService;

    /**
     * 访问密钥
     */
    @Value("${qiniu.key.access}")
    private String accessKey;

    /**
     * 秘密密钥
     */
    @Value("${qiniu.key.secret}")
    private String secretKey;

    /**
     * headerBucket name
     */
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    /**
     * headerBucket url
     */
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    /**
     * 得到设置页面
     *
     * @return {@link String}
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    /**
     * 更新头像路径
     * 上传七牛云新增
     *
     * @param fileName 文件名称
     * @return {@link String}
     */
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String uploadHeaderUrl(String fileName){
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 上传头像
     * 暂且废弃
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
        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            multipartFile.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        // 更新当前文件路径
        User user = hostHolder.getUser();
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headUrl);
        return "redirect:/index";
    }

    /**
     * 获取头像
     * 暂且废弃
     *
     * @param filename 文件名
     * @param response 响应
     */
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        // 文件后缀
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filename)) {
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

    /**
     * 获取个人信息页面
     *
     * @param userId 用户id
     * @param model  模型
     * @return {@link String}
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

    /**
     * 得到我的帖子
     *
     * @param userId 用户id
     * @param page   页面
     * @param model  模型
     * @return {@link String}
     */
    @RequestMapping(path = "/myPost/{userId}", method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setLimit(5);
        page.setPath("/user/myPost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        // 帖子列表
        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> discussVoList = new ArrayList<>();
        if (discussPostList != null) {
            for (DiscussPost discussPost : discussPostList) {
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost", discussPost);
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                discussVoList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussVoList);
        return "/site/my-post";
    }

    /**
     * 得到我的回复
     *
     * @param userId 用户id
     * @param page   页面
     * @param model  模型
     * @return {@link String}
     */
    @RequestMapping(path = "/myReply/{userId}", method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setLimit(5);
        page.setPath("/user/myReply/" + userId);
        page.setRows(commentService.findCountByUser(userId));

        // 回复列表
        List<Comment> commentList = commentService.findCommentsByUser(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost", discussPost);
                commentVoList.add(map);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/my-reply";
    }
}
