package com.yu.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

/**
 * wk图像删除工作
 *
 * @author yu
 * @date 2022/05/31
 */
public class WkImageDeleteJob implements Job {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(WkImageDeleteJob.class);

    /**
     * wk图像存储
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 执行
     *
     * @param jobExecutionContext 工作执行上下文
     * @throws JobExecutionException 作业执行异常
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        File[] files = new File(wkImageStorage).listFiles();
        if (files == null || files.length == 0) {
            logger.info("没有WK图片，任务取消！");
            return;
        }
        for (File file : files) {
            // 删除一分钟之前创建的图片
            if (System.currentTimeMillis() - file.lastModified() > 60 * 1000) {
                logger.info("删除WK图片：" + file.getName());
                file.delete();
            }
        }
    }
}
