package com.kunpeng.metal_filament_inspection;

import cn.hutool.crypto.digest.BCrypt;
import com.kunpeng.metal_filament_inspection.controller.TestController;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class MetalFilamentInspectionApplicationTests {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private IUserService userService;
    @Autowired
    private TestController testController;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private IWireMaterialService wireMaterialService;
    @Test
    void contextLoads() {
        log.info("{}",testController.test());
    }
//    @Test
//    void testParse(){
//        Claims claims = jwtUtil.parseToken("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiLnjovlvLoiLCJ1c2VySWQiOjIzLCJleHAiOjE3" +
//                "ODE2NzY5ODZ9.UXyFTVCNPgVIS9foeBdsopJH7PzoiQJ4Akhjr_5J-i2gjqhFXUax1K2JvSk3CGL4");
//        Long userId = claims.get("userId", Long.class);
//        log.info("用户ID: {}", userId);
//    }
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
                .account("wangqiang@example.com")
                .passwd("123456")
                .build();
        log.info("{}",userService.login(login));
    }
    @Test
    void testId(){
        Long l = idWorker.generateId("test");
        log.info("{}",l);
    }
//    @Test
//    void testIdWMBatchNumber(){
//        for (int i = 0; i < 300; i++) {
//            WireMaterialDTO wireMaterial = WireMaterialDTO.builder()
//                    .newBatchNumber(idWorker.generateId(SystemConstants.WIRE_MATERIAL_PREFIX))
//                    .build();
//            String i1 = String.format("%03d", i);
//            String batchNumber = "BATCH-20260610-0" + i1;
//            wireMaterialService.updateByBatchNumber(wireMaterial,batchNumber);
//        }
//    }
//    @Test
//    void testUpdateBR() {
//        for (int i = 3; i < 62641037155237990L-62641032860270627L; i++) {
//            Long id = 62641032860270627L + i;
//            WireMaterial entity = wireMaterialService.getById(id);
//            if (entity != null) {
//                // 设置批次（循环1-10），卷序（循环1-30）按需设计
//                // 比如批次 = (i / 30) + 1，卷序 = (i % 30) + 1
//                int batch = (i / 30) + 1;
//                int roll = (i % 30) + 1;
//                entity.setBatchNo((long) batch);
//                entity.setRollNo((long) roll);
//                wireMaterialService.updateById(entity); // 使用updateById
//          }
//       }
//  }
}
