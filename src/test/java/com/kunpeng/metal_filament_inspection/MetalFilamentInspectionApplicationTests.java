package com.kunpeng.metal_filament_inspection;

import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@Slf4j
@SpringBootTest
class MetalFilamentInspectionApplicationTests {
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
    }
    @Test
    void testParse(){
        Claims claims = jwtUtil.parseToken("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiLnjovlvLoiLCJ1c2VySWQiOjIzLCJleHAiOjE3ODE2NzY5ODZ9.UXyFTVCNPgVIS9foeBdsopJH7PzoiQJ4Akhjr_5J-i2gjqhFXUax1K2JvSk3CGL4");
        Long userId = claims.get("userId", Long.class);
        log.info("用户ID: {}", userId);
    }
}
