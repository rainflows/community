package com.yu.dao;

import com.yu.pojo.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * 登录票映射器
 *
 * @author yu
 * @date 2022/05/11
 */
@Mapper
public interface LoginTicketMapper {
    /**
     * 插入登录票
     *
     * @param loginTicket 登录机票
     * @return int
     */
    @Insert("insert into community.login_ticket(user_id,ticket,status,expired) values (#{userId},#{ticket},#{status},#{expired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 选择机票
     *
     * @param ticket 票
     * @return {@link LoginTicket}
     */
    @Select("select * from community.login_ticket where ticket = #{ticket}")
    LoginTicket selectByTicket(String ticket);

    /**
     * 更新状态
     *
     * @param ticket 票
     * @param status 状态
     * @return int
     */
    @Update("update community.login_ticket set status = #{status} where ticket = #{ticket}")
    int updateStatus(String ticket, int status);
}
