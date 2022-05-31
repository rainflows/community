package com.yu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 线程池配置
 *
 * @author yu
 * @date 2022/05/29
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
