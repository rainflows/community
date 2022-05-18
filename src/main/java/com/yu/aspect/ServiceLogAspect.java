package com.yu.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 服务日志切面:统一记录日志
 *
 * @author yu
 * @date 2022/05/17
 */
@Component
@Aspect
public class ServiceLogAspect {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * 之前
     *
     * @param joinPoint 连接点
     */
    @Before("execution(* com.yu.service.*.*(..))")
    public void before(JoinPoint joinPoint) {
        // 用户x.x.x.x(ip)在xxx(时刻)访问了xxx(方法)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s]在[%s]访问了[%s].", ip, now, target));
    }
}
