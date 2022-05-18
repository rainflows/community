package com.yu.utils;

import com.yu.pojo.User;
import org.springframework.stereotype.Component;

/**
 * 用户持有者
 * 持有用户信息，用于代替session对象
 *
 * @author yu
 * @date 2022/05/12
 */
@Component
public class HostHolder {
    /**
     * 用户
     */
    private ThreadLocal<User> users = new ThreadLocal<>();

    /**
     * 设置用户
     *
     * @param user 用户
     */
    public void setUser(User user) {
        users.set(user);
    }

    /**
     * 获取用户
     *
     * @return {@link User}
     */
    public User getUser() {
        return users.get();
    }

    /**
     * 清除
     */
    public void clear() {
        users.remove();
    }
}
