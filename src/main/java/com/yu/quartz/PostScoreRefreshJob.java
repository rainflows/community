package com.yu.quartz;

import com.yu.pojo.DiscussPost;
import com.yu.service.DiscussPostService;
import com.yu.service.ElasticsearchService;
import com.yu.service.LikeService;
import com.yu.utils.CommunityConstant;
import com.yu.utils.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 帖子分数更新工作
 *
 * @author yu
 * @date 2022/05/30
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    /**
     * 日志记录器
     */
    private static Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    /**
     * Redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 讨论帖服务
     */
    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 点赞服务
     */
    @Autowired
    private LikeService likeService;

    /**
     * elasticsearch服务
     */
    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 牛客纪元
     */
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    /**
     * 执行
     *
     * @param jobExecutionContext 工作执行上下文
     * @throws JobExecutionException 作业执行异常
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子！");
            return;
        }
        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务完毕] 帖子分数更新完毕！");
    }

    /**
     * 更新
     *
     * @param postId post id
     */
    public void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }
        // 是否为精华帖
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        // 计算权重
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子分数 + 距离天数
        double score = Math.log10(Math.max(weight, 1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
