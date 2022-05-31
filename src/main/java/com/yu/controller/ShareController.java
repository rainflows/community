package com.yu.controller;

import com.yu.event.EventProducer;
import com.yu.pojo.Event;
import com.yu.utils.CommunityConstant;
import com.yu.utils.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 分享控制器
 *
 * @author yu
 * @date 2022/05/30
 */
@Controller
public class ShareController implements CommunityConstant {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    /**
     * 事件生成器
     */
    @Autowired
    private EventProducer eventProducer;

    /**
     * 域
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 上下文路径
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * wk图像存储
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * shareBucket url
     */
    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    /**
     * 分享
     *
     * @param htmlUrl htmlurl
     * @return {@link String}
     */
    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        // 文件名
        String fileName = CommunityUtil.generateUUID();
        // 异步生成长图
        Event event = new Event();
        event.setTopic(TOPIC_SHARE);
        event.setData("htmlUrl", htmlUrl);
        event.setData("fileName", fileName);
        event.setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
//        map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    /**
     * 得到分享图片
     * 暂且废弃
     *
     * @param fileName 文件名称
     * @param response 响应
     */
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }
    }
}
