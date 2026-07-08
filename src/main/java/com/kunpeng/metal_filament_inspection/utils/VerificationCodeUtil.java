package com.kunpeng.metal_filament_inspection.utils;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeUtil {

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成并发送验证码
     * @param email 收件人邮箱
     * @return 生成的验证码（用于后续校验）
     */
    public String sendVerificationCode(String email) {
        // 1. 生成6位随机验证码
        String code = RandomUtil.randomString(6);
        // 2. 组装邮件内容
        String subject = "【金属微细线材综合检测平台】邮箱验证码";
        String content = "您好，您的验证码为：" + code + "，有效期为3分钟。请勿泄露给他人。";
        // 3. 发送邮件
        emailUtil.sendSimpleEmail(email, subject, content);
        // 4. 缓存验证码
        String key = SystemConstants.MAIL_SEND_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(key,code,180, TimeUnit.SECONDS);
        return code;
    }
    /**
     * 生成并发送验证码
     * @param email 收件人邮箱
     * @return 生成的验证码（用于后续校验）
     */
    public String sendVerificationCode(String email,String userZone,String salt) {
        // 1. 生成6位随机验证码
        String code = RandomUtil.randomString(6);
        // 2. 组装邮件内容
        String subject = "【金属微细线材综合检测平台】"+userZone+"验证码";
        String content = "您好，您的验证码为：" + code + "，有效期为3分钟。请勿泄露给他人。";
        // 3. 发送邮件
        emailUtil.sendSimpleEmail(email, subject, content);
        // 4. 缓存验证码
        String key = SystemConstants.MAIL_SEND_CODE_KEY + email +salt;
        stringRedisTemplate.opsForValue().set(key,code,180, TimeUnit.SECONDS);
        return code;
    }

    /**
     * 校验验证码
     * @param email 用户邮箱
     * @param code  用户输入的验证码
     * @return 是否校验通过
     */
    public boolean verifyCode(String email, String code) {
        String key = SystemConstants.MAIL_SEND_CODE_KEY + email;
        String cacheCode = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        return cacheCode != null && cacheCode.equalsIgnoreCase(code);
    }
    /**
     * 校验验证码
     * @param email 用户邮箱
     * @param code  用户输入的验证码
     * @return 是否校验通过
     */
    public boolean verifyCode(String email, String code ,String salt) {
        String key = SystemConstants.MAIL_SEND_CODE_KEY + email + salt;
        String cacheCode = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        return cacheCode != null && cacheCode.equals(code);
    }

    public String sendLoginEmailCode(String email) {
        // 1. 生成6位随机验证码
        String code = RandomUtil.randomString(6);
        // 2. 组装邮件内容
        String subject = "【金属微细线材综合检测平台】邮箱登录验证码";
        String content = "您好，您的登录验证码为：" + code + "，有效期为3分钟。请勿泄露给他人。";
        // 3. 发送邮件
        emailUtil.sendSimpleEmail(email, subject, content);
        // 4. 缓存验证码
        String key = SystemConstants.MAIL_SEND_LOGIN_CODE_KEY + email;
        stringRedisTemplate.opsForValue().set(key,code,180, TimeUnit.SECONDS);
        return code;
    }
    /**
     * 校验登录验证码
     * @param email 用户邮箱
     * @param code  用户输入的验证码
     * @return 是否校验通过
     */
    public boolean verifyLoginCode(String email, String code) {
        String key = SystemConstants.MAIL_SEND_LOGIN_CODE_KEY + email;
        String cacheCode = stringRedisTemplate.opsForValue().get(key);
        stringRedisTemplate.delete(key);
        return cacheCode != null && cacheCode.equals(code);
    }
}