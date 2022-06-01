package com.yu.actuator;

import com.yu.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库端点
 * 监控数据库
 *
 * @author yu
 * @date 2022/05/31
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndPoint {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndPoint.class);

    /**
     * 数据源
     */
    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return CommunityUtil.getJSONString(0, "获取连接成功!");
        } catch (SQLException e) {
            logger.error("获取连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取连接失败！");
        }
    }
}
