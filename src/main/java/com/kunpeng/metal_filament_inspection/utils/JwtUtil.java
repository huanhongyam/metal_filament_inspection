package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    private SecretKey getKey() {
        // 指定签名的时候使用的签名算法，也就是header那部分
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    /**
     * 根据用户信息生成 JWT Token
     * @param userId   用户ID
     * @param username  用户用户名
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .expiration(expiration)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 Token，返回 Claims
     * @param token JWT Token
     * @return Claims 对象，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 Token 是否有效
     * @param token JWT Token
     * @return true=有效，false=无效或过期
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        return !claims.getExpiration().before(new Date());
    }

    /**
     * 从 Token 中提取用户名
     * @param token JWT Token
     * @return 用户名，解析失败返回 null
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 Token 中提取用户ID
     * @param token JWT Token
     * @return 用户ID，解析失败返回 null
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

}