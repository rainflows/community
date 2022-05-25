package com.yu.controller;

import com.github.pagehelper.PageInfo;
import com.yu.pojo.DiscussPost;
import com.yu.pojo.Page;
import com.yu.service.ElasticsearchService;
import com.yu.service.LikeService;
import com.yu.service.UserService;
import com.yu.utils.CommunityConstant;
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
 * 搜索控制器
 *
 * @author yu
 * @date 2022/05/24
 */
@Controller
public class SearchController implements CommunityConstant {

    /**
     * elasticsearch服务
     */
    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 用户服务
     */
    @Autowired
    private UserService userService;

    /**
     * 点赞服务
     */
    @Autowired
    private LikeService likeService;

    /**
     * 搜索
     *
     * @param keyword 关键字
     * @param page    页面
     * @param model   模型
     * @return {@link String}
     */
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 根据关键词搜索帖子
        List<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword);
        // 搜索结果进行分页处理
        PageInfo<DiscussPost> queryPageInfo = elasticsearchService.queryPageInfo(searchResult, page.getCurrent(), page.getLimit());
        List<DiscussPost> pageInfoList = queryPageInfo.getList();

        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (!pageInfoList.isEmpty()) {
            for (DiscussPost post : pageInfoList) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult.isEmpty() ? 0 : searchResult.size());

        return "/site/search";
    }
}
