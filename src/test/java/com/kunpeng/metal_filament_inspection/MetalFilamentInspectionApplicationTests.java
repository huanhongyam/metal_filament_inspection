package com.kunpeng.metal_filament_inspection;

import cn.hutool.crypto.digest.BCrypt;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.service.IUserService;
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
    @Autowired
    private IUserService userService;

    @Test
    void contextLoads() {
    }
    @Test
    void testParse(){
        Claims claims = jwtUtil.parseToken("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiLnjovlvLoiLCJ1c2VySWQiOjIzLCJleHAiOjE3" +
                "ODE2NzY5ODZ9.UXyFTVCNPgVIS9foeBdsopJH7PzoiQJ4Akhjr_5J-i2gjqhFXUax1K2JvSk3CGL4");
        Long userId = claims.get("userId", Long.class);
        log.info("用户ID: {}", userId);
    }
    @Test
    void testPlainBcrypt() {
        String gensalt = BCrypt.gensalt(10);
        log.info("生成的盐值：{}",gensalt);
        String hash =  BCrypt.hashpw("123456", gensalt);
        log.info("生成的哈希：{}", hash); // 输出长度固定 60
        boolean checkpw = BCrypt.checkpw("123456", hash);
        log.info(String.valueOf(checkpw));
    }
    @Test
    void Login(){
        LoginFormDTO login = LoginFormDTO.builder()
                .account("mali@example.com")
                .passwd("123456")
                .build();
        userService.login(login);
    }
}
