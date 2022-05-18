package com.yu.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 社区工具
 *
 * @author yu
 * @date 2022/05/10
 */
public class CommunityUtil {

    /**
     * uuid生成
     *
     * @return {@link String}
     */
    public static final String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * md5
     *
     * @param key 关键
     * @return {@link String}
     */
    public static String MD5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 得到jsonstring
     *
     * @param code 代码
     * @param msg  信息
     * @param map  地图
     * @return {@link String}
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    /**
     * 得到jsonstring
     *
     * @param code 代码
     * @param msg  信息
     * @return {@link String}
     */
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    /**
     * 得到jsonstring
     *
     * @param code 代码
     * @return {@link String}
     */
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
