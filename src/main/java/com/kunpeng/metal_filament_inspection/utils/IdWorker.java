package com.kunpeng.metal_filament_inspection.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class IdWorker {
    private static final long BEGIN_TIMESTAMP = 1767225600L;
    private static final int COUNT_BITS = 32;
    private StringRedisTemplate stringRedisTemplate;
    // 构造器注入
    public IdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public Long generateId(String keyPrefix){
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long endSecond = nowSecond-BEGIN_TIMESTAMP;
        // 2.生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long increment = stringRedisTemplate.opsForValue().increment(keyPrefix + ":" + date);
        // 3.拼接并返回
        return endSecond<<COUNT_BITS | increment;
    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        long epochSecond = localDateTime.toEpochSecond(ZoneOffset.UTC);
        System.out.println("second = " + epochSecond);
    }
}
