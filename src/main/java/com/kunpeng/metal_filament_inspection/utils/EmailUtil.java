package com.kunpeng.metal_filament_inspection.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class EmailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送纯文本邮件
     * @param toEmail   收件人邮箱
     * @param subject   邮件主题
     * @param content   邮件内容
     */
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("邮件发送成功！收件人：{}", toEmail);
        } catch (Exception e) {
            log.info("邮件发送失败：{}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}