package com.yu.controller;

import com.yu.pojo.DiscussPost;
import com.yu.pojo.Page;
import com.yu.pojo.User;
import com.yu.service.DiscussPostService;
import com.yu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家控制器
 *
 * @author shah
 * @date 2022/05/09
 */
@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;
    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 得到索引页
     *
     * @param model 模型
     * @return {@link String}
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "index";
    }
}
