package com.yu.service;

import com.yu.dao.UserMapper;
import com.yu.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 *
 * @author shah
 * @date 2022/05/09
 */
@Service
public class UserService {
    /**
     * 用户映射器
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 找到用户id
     *
     * @param id id
     * @return {@link User}
     */
    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
