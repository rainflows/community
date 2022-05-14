package com.yu.utils;

/**
 * 社区常数
 *
 * @author yu
 * @date 2022/05/11
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认登录凭证过期时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记得状态的登录过期时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
}
