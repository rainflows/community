package com.yu.config;

import com.yu.controller.interceptor.LoginRequiredInterceptor;
import com.yu.controller.interceptor.LoginTicketInterceptor;
import com.yu.controller.interceptor.MessageInterceptor;
import com.yu.controller.interceptor.StatisticsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web mvc配置
 *
 * @author yu
 * @date 2022/05/12
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 登录凭证拦截器
     */
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    /**
     * 登录需要拦截器
     */
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    /**
     * 消息拦截器
     */
    @Autowired
    private MessageInterceptor messageInterceptor;

    /**
     * 统计数据拦截器
     */
    @Autowired
    private StatisticsInterceptor statisticsInterceptor;

    /**
     * 添加拦截器
     *
     * @param registry 注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(statisticsInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }

}
