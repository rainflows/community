package com.yu;

import java.io.IOException;

/**
 * Wk测试
 *
 * @author yu
 * @date 2022/05/30
 */
public class WkTest {

    public static void main(String[] args) {
        String cmd = "d:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com d:/javaweb/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
