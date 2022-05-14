package com.yu.dao;

import com.yu.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户映射器
 *
 * @author yu
 * @date 2022/05/09
 */
@Mapper
public interface UserMapper {
    /**
     * 选择通过id
     *
     * @param id id
     * @return {@link User}
     */
    User selectById(int id);

    /**
     * 选择名字
     *
     * @param username 用户名
     * @return {@link User}
     */
    User selectByName(String username);

    /**
     * 选择通过电子邮件
     *
     * @param email 电子邮件
     * @return {@link User}
     */
    User selectByEmail(String email);

    /**
     * 插入用户
     *
     * @param user 用户
     * @return int
     */
    int insertUser(User user);

    /**
     * 更新状态
     *
     * @param id     id
     * @param status 状态
     * @return int
     */
    int updateStatus(int id, int status);

    /**
     * 更新头
     *
     * @param id        id
     * @param headerUrl 头url
     * @return int
     */
    int updateHeader(int id, String headerUrl);

    /**
     * 更新密码
     *
     * @param id       id
     * @param password 密码
     * @return int
     */
    int updatePassword(int id, String password);
}
