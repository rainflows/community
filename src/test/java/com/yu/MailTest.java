package com.yu;

import com.yu.utils.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 邮件测试
 *
 * @author yu
 * @date 2022/05/10
 */
@SpringBootTest
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMail() {
        mailClient.sendMail("1907710439@qq.com", "test", "miao~");
    }
}
