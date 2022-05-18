package com.yu;

import com.yu.utils.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 敏感测试
 *
 * @author yu
 * @date 2022/05/16
 */
@SpringBootTest
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test() {
        String text = "来啊，快活啊，这里可以吸毒，可以赌博，可以嫖娼，快活就来快活林！";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
