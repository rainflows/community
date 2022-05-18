package com.yu.controller.advice;

import com.yu.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异常通知:统一处理异常
 *
 * @author yu
 * @date 2022/05/17
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * 处理异常
     *
     * @param e        e
     * @param request  请求
     * @param response 响应
     */
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        // 判断是普通请求还是异步请求
        String header = request.getHeader("x-requested-with");
        // 异步请求
        if ("XMLHttpRequest".equals(header)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        } else {
            // 普通请求
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
