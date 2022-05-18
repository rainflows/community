package com.yu.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 消息
 *
 * @author yu
 * @date 2022/05/17
 */
@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
