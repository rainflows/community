package com.yu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件客户端
 *
 * @author yu
 * @date 2022/05/10
 */
@Component
public class MailClient {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    /**
     * 邮件发送者
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * 从
     */
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件
     *
     * @param to      来
     * @param subject 主题
     * @param content 内容
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }
    }
}
