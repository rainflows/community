package com.yu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * wk配置
 *
 * @author yu
 * @date 2022/05/30
 */
@Configuration
public class WkConfig {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    /**
     * wk图像存储
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 创建WK图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("创建WK图片目录：" + wkImageStorage);
        }
    }
}
