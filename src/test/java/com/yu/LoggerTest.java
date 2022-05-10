package com.yu;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 记录器测试
 *
 * @author shah
 * @date 2022/05/10
 */
@SpringBootTest
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);
    @Test
    public void testLogger(){
        System.out.println(logger.getName());
        logger.debug("debug.log");
        logger.info("info.log");
        logger.warn("warn.log");
        logger.error("error.log");
    }
}
