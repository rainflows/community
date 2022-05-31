package com.yu.controller.interceptor;

import com.yu.pojo.User;
import com.yu.service.StatisticsService;
import com.yu.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统计数据拦截器
 *
 * @author yu
 * @date 2022/05/28
 */
@Component
public class StatisticsInterceptor implements HandlerInterceptor {

    /**
     * 统计服务
     */
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 用户持有者
     */
    @Autowired
    private HostHolder hostHolder;

    /**
     * 前处理
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理程序
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        statisticsService.recordUV(ip);
        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            statisticsService.recordDAU(user.getId());
        }
        return true;
    }
}
