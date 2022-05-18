package com.yu.utils;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * 敏感过滤器
 *
 * @author yu
 * @date 2022/05/14
 */
@Component
public class SensitiveFilter {
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    /**
     * 更换符
     */
    private static final String REPLACEMENT = "**";

    /**
     * 根节点
     */
    private TrieNode root = new TrieNode();

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        try (
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream("sensitiveWord.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    /**
     * 添加关键字
     *
     * @param keyword 关键字
     */
    private void addKeyword(String keyword) {
        TrieNode temp = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = temp.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                temp.addSubNode(c, subNode);
            }
            temp = subNode;

            if (i == keyword.length() - 1) {
                temp.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤器
     * 过滤敏感词
     *
     * @param text 文本
     * @return {@link String}
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode temp = root;
        int start = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                if (temp == root) {
                    sb.append(c);
                    start++;
                }
                position++;
                continue;
            }
            // 检查下级节点
            temp = temp.getSubNode(c);
            if (temp == null) {
                // start开头的字符串不是敏感词
                sb.append(text.charAt(start));
                position = ++start;
                temp = root;
            } else if (temp.isKeyWordEnd()) {
                // 发现敏感词，替换start~position间的字符
                sb.append(REPLACEMENT);
                start = ++position;
                temp = root;
            } else if (position == text.length() - 1) {
                // 特殊情况，position到达字符串末尾，再次判断start+1~position之间的字符
                sb.append(text.charAt(start));
                position = ++start;
                temp = root;
            } else {
                // 检查下一个字符
                position++;
            }
        }
        sb.append(text.substring(start));
        return sb.toString();
    }

    /**
     * 是符号
     *
     * @param c c
     * @return boolean
     */
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}

/**
 * 前缀树节点
 *
 * @author yu
 * @date 2022/05/14
 */
class TrieNode {
    /**
     * 关键词结束标识
     */
    private boolean isKeyWordEnd = false;
    /**
     * 子节点(key为下级字符,value为下级节点)
     */
    private Map<Character, TrieNode> subNode = new HashMap<>();

    /**
     * 添加子节点
     */
    public void addSubNode(Character c, TrieNode node) {
        subNode.put(c, node);
    }

    /**
     * 获取子节点
     */
    public TrieNode getSubNode(Character c) {
        return subNode.get(c);
    }

    /**
     * 关键字结束
     *
     * @return boolean
     */
    public boolean isKeyWordEnd() {
        return isKeyWordEnd;
    }

    /**
     * 设置关键字结束
     *
     * @param keyWordEnd 关键字结束
     */
    public void setKeyWordEnd(boolean keyWordEnd) {
        isKeyWordEnd = keyWordEnd;
    }

    /**
     * 获取子节点
     *
     * @return {@link Map}<{@link Character}, {@link TrieNode}>
     */
    public Map<Character, TrieNode> getSubNode() {
        return subNode;
    }

    /**
     * 设置子节点
     *
     * @param subNode 子节点
     */
    public void setSubNode(Map<Character, TrieNode> subNode) {
        this.subNode = subNode;
    }
}