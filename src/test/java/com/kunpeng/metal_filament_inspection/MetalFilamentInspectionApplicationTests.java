package com.kunpeng.metal_filament_inspection;

import cn.hutool.crypto.digest.BCrypt;
import com.kunpeng.metal_filament_inspection.controller.TestController;
import com.kunpeng.metal_filament_inspection.domain.dto.LoginFormDTO;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import com.kunpeng.metal_filament_inspection.utils.QiniuUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    @Autowired
    private QiniuUploadUtil qiniuUploadUtil;
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
    @Test
    void getAgentJwt(){
        String s = jwtUtil.generateToken(100L, "agent");
        log.info(s);
    }
    @Test
    public void testUploadLocalImage() throws IOException {
        // 1. 🔧 替换为您的本地绝对路径（比如 Windows: C:/Users/xxx/Desktop/test.jpg）
        String absolutePath = "C:\\Users\\z\\Pictures\\Saved Pictures\\微信图片.jpg";

        // 2. 读取文件字节流
        byte[] fileBytes = Files.readAllBytes(Paths.get(absolutePath));

        // 3. 提取文件名
        String fileName = Paths.get(absolutePath).getFileName().toString();

        // 4. 核心：将本地文件包装成 MultipartFile（Mock实现）
        // 参数说明：参数1-表单字段名(随意)，参数2-原始文件名，参数3-MIME类型，参数4-字节数组
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                fileName,
                "image/jpeg",           // 如果是png，改为"image/png"
                fileBytes
        );

        // 5. 🚀 调用您的工具类方法（与您写的代码完全一致）
        String cloudUrl = qiniuUploadUtil.uploadImage(multipartFile);

        // 6. 打印结果并断言
        System.out.println("✅ 上传成功，访问地址: " + cloudUrl);
    }
}
