package com.kunpeng.metal_filament_inspection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT签名密钥
     */
    private String secret;

    /**
     * 登录过期时间（小时）
     */
    private int expiration;
}