package com.yu.config;

import com.yu.quartz.PostScoreRefreshJob;
import com.yu.quartz.WkImageDeleteJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Quartz配置
 *
 * @author yu
 * @date 2022/05/29
 */
@Configuration
public class QuartzConfig {

    /**
     * 帖子分数刷新工作细节
     * <p>
     * FactoryBean简化Bean的实例化过程
     * 1.通过FactoryBean封装Bean的实例化过程
     * 2.将FactoryBean装配到Spring容器中
     * 3.将FactoryBean注入给其他Bean
     * 4.该Bean得到的是FactoryBean管理的对象实例
     *
     * @return {@link JobDetailFactoryBean}
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /**
     * 帖子分数刷新触发器
     *
     * @param postScoreRefreshJobDetail 帖子分数刷新工作细节
     * @return {@link SimpleTriggerFactoryBean}
     */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    /**
     * wk图像删除工作
     *
     * @return {@link JobDetailFactoryBean}
     */
    @Bean
    public JobDetailFactoryBean wkImageDeleteJob() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(WkImageDeleteJob.class);
        factoryBean.setName("wkImageDeleteJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /**
     * wk图像删除触发器
     *
     * @param wkImageDeleteJob wk图像删除工作
     * @return {@link SimpleTriggerFactoryBean}
     */
    @Bean
    public SimpleTriggerFactoryBean wkImageDeleteTrigger(JobDetail wkImageDeleteJob){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(wkImageDeleteJob);
        factoryBean.setName("wkImageDeleteTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 4);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
