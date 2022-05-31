package com.yu.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yu.dao.DiscussPostMapper;
import com.yu.pojo.DiscussPost;
import com.yu.utils.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 讨论帖服务
 *
 * @author yu
 * @date 2022/05/09
 */
@Service
public class DiscussPostService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    /**
     * 讨论后映射器
     */
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 敏感过滤器
     */
    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 最大尺寸
     */
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    /**
     * 到期秒
     */
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    /**
     * 帖子列表缓存
     * Caffeine核心接口：Cache,LoadingCache,AsyncLoadingCache
     */
    private LoadingCache<String, List<DiscussPost>> postListCache;

    /**
     * 帖子总数缓存
     */
    private LoadingCache<Integer, Integer> postRowsCache;

    /**
     * 初始化
     * 优化
     */
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String s) throws Exception {
                        if (s == null || s.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        String[] params = s.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        Integer offset = Integer.valueOf(params[0]);
                        Integer limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 0);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer integer) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(integer);
                    }
                });
    }

    /**
     * 找到讨论帖子
     *
     * @param userId 用户id
     * @param offset 抵消
     * @param limit  限制
     * @return {@link List}<{@link DiscussPost}>
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 利用缓存进行优化
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");

        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 找到讨论帖行数
     *
     * @param userId 用户id
     * @return int
     */
    public int findDiscussPostRows(int userId) {
        // 利用缓存进行优化
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");

        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 添加讨论后
     *
     * @param discussPost 讨论后
     * @return int
     */
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 找到讨论帖子id
     *
     * @param id id
     * @return {@link DiscussPost}
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新评论数
     *
     * @param id           id
     * @param commentCount 评论数
     * @return int
     */
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     * 更新类型
     *
     * @param id   id
     * @param type 类型
     * @return int
     */
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 更新状态
     *
     * @param id     id
     * @param status 状态
     * @return int
     */
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 更新分数
     *
     * @param id    id
     * @param score 分数
     * @return int
     */
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
