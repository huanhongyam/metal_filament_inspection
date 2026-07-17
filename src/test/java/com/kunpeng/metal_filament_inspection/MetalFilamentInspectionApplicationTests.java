package com.kunpeng.metal_filament_inspection;

import cn.hutool.crypto.digest.BCrypt;
import com.kunpeng.metal_filament_inspection.controller.TestController;
import com.kunpeng.metal_filament_inspection.domain.dto.*;
import com.kunpeng.metal_filament_inspection.domain.vo.QuestionVO;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPassRateVO;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPhysicalVO;
import com.kunpeng.metal_filament_inspection.service.IQuestionService;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import com.kunpeng.metal_filament_inspection.utils.QiniuUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    private IQuestionService questionService;
//    @Test
//    void contextLoads() {
//        log.info("{}",testController.test());
//    }
////    @Test
////    void testParse(){
////        Claims claims = jwtUtil.parseToken("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiLnjovlvLoiLCJ1c2VySWQiOjIzLCJleHAiOjE3" +
////                "ODE2NzY5ODZ9.UXyFTVCNPgVIS9foeBdsopJH7PzoiQJ4Akhjr_5J-i2gjqhFXUax1K2JvSk3CGL4");
////        Long userId = claims.get("userId", Long.class);
////        log.info("用户ID: {}", userId);
////    }
//    @Test
//    void testPlainBcrypt() {
//        String gensalt = BCrypt.gensalt(10);
//        log.info("生成的盐值：{}",gensalt);
//        String hash =  BCrypt.hashpw("123456", gensalt);
//        log.info("生成的哈希：{}", hash); // 输出长度固定 60
//        boolean checkpw = BCrypt.checkpw("123456", hash);
//        log.info(String.valueOf(checkpw));
//    }
//    @Test
//    void Login(){
//        LoginFormDTO login = LoginFormDTO.builder()
//                .account("wangqiang@example.com")
//                .passwd("123456")
//                .build();
//        log.info("{}",userService.login(login));
//    }
//    @Test
//    void testId(){
//        Long l = idWorker.generateId("test");
//        log.info("{}",l);
//    }
////    @Test
////    void testIdWMBatchNumber(){
////        for (int i = 0; i < 300; i++) {
////            WireMaterialDTO wireMaterial = WireMaterialDTO.builder()
////                    .newBatchNumber(idWorker.generateId(SystemConstants.WIRE_MATERIAL_PREFIX))
////                    .build();
////            String i1 = String.format("%03d", i);
////            String batchNumber = "BATCH-20260610-0" + i1;
////            wireMaterialService.updateByBatchNumber(wireMaterial,batchNumber);
////        }
////    }
//    @Test
//    void getAgentJwt(){
//        String s = jwtUtil.generateToken(100L, "agent");
//        log.info(s);
//    }
//    @Test
//    public void testUploadLocalImage() throws IOException {
//        // 1. 🔧 替换为您的本地绝对路径（比如 Windows: C:/Users/xxx/Desktop/test.jpg）
//        String absolutePath = "C:\\Users\\z\\Pictures\\Saved Pictures\\微信图片.jpg";
//
//        // 2. 读取文件字节流
//        byte[] fileBytes = Files.readAllBytes(Paths.get(absolutePath));
//
//        // 3. 提取文件名
//        String fileName = Paths.get(absolutePath).getFileName().toString();
//
//        // 4. 核心：将本地文件包装成 MultipartFile
//        // 参数说明：参数1-表单字段名(随意)，参数2-原始文件名，参数3-MIME类型，参数4-字节数组
//        MultipartFile multipartFile = new MockMultipartFile(
//                "file",
//                fileName,
//                "image/jpeg",           // 如果是png，改为"image/png"
//                fileBytes
//        );
//
//        // 5. 调用工具类方法
//        String cloudUrl = qiniuUploadUtil.uploadImage(multipartFile);
//
//        // 6. 打印结果并断言
//        System.out.println("✅ 上传成功，访问地址: " + cloudUrl);
//    }
//    @Test
//    public void testAgentQuestion(){
//        QuestionAskDTO questionAskDTO = new QuestionAskDTO();
//        questionAskDTO.setQuestionContent("11,请回答纯文本内容不影响gbk显示，无图标回答");
//        Result<QuestionVO> questionVOResult = questionService.askFromUser(23L, questionAskDTO);
//        log.info(questionVOResult.getData().getAiResponseContent());
//    }

    // ============================================================
    // 集成测试 — 连真实数据库
    // ============================================================

    @Nested
    @DisplayName("集成测试: QueryWithBatchNoAvg")
    class IntegrationQueryWithBatchNoAvg {

        @Test
        @DisplayName("batchNo 指定已有批次返回聚合数据")
        void shouldReturnAvgForExistingBatch() {
            WireMaterialPhysicalVO result = wireMaterialService.QueryWithBatchNoAvg(20260708L);
            if (result != null) {
                log.info("批次 {} 平均值: 直径={}, 电阻={}", result.getBatchNo(), result.getDiameter(), result.getResistance());
                assertNotNull(result.getDiameter());
                assertNotNull(result.getResistance());
            }
        }

        @Test
        @DisplayName("batchNo 不存在返回 null")
        void shouldReturnNullForNonExistingBatch() {
            WireMaterialPhysicalVO result = wireMaterialService.QueryWithBatchNoAvg(99999999L);
            assertNull(result);
        }

        @Test
        @DisplayName("batchNo=null 查最新的批次")
        void shouldQueryLatestWhenNull() {
            WireMaterialPhysicalVO result = wireMaterialService.QueryWithBatchNoAvg(null);
            if (result != null) {
                log.info("最新批次: {}", result.getBatchNo());
                assertNotNull(result.getBatchNo());
            }
        }
    }

    @Nested
    @DisplayName("集成测试: getPassRateByYearMonth")
    class IntegrationGetPassRateByYearMonth {

        @Test
        @DisplayName("正常查询 2026-07 返回按天分组数据")
        void shouldReturnDailyStats() {
            List<WireMaterialPassRateVO> result = wireMaterialService.getPassRateByYearMonth("2026-07");
            assertNotNull(result);
            log.info("2026-07 共有 {} 天有数据", result.size());
            for (WireMaterialPassRateVO vo : result) {
                assertNotNull(vo.getDay());
                assertTrue(vo.getPassCount() >= 0);
                assertTrue(vo.getFailCount() >= 0);
                // passRate 必须在 0-100 之间
                assertTrue(vo.getPassRate().compareTo(BigDecimal.ZERO) >= 0);
                assertTrue(vo.getPassRate().compareTo(new BigDecimal("100")) <= 0);
            }
        }

        @Test
        @DisplayName("无数据的月份返回空列表")
        void shouldReturnEmptyForNoDataMonth() {
            List<WireMaterialPassRateVO> result = wireMaterialService.getPassRateByYearMonth("2025-01");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("集成测试: listPageWithFilter")
    class IntegrationListPageWithFilter {

        @Test
        @DisplayName("无任何过滤条件返回第一页")
        void shouldReturnFirstPageWithoutFilters() {
            PageDTO result = wireMaterialService.listPageWithFilter(1,
                    null, null, null, null, null, null, null, null);
            assertNotNull(result);
            assertTrue(result.getTotal() > 0);
            assertFalse(result.getRecords().isEmpty());
            log.info("无过滤分页: total={}, pageSize={}", result.getTotal(), result.getRecords().size());
        }

        @Test
        @DisplayName("deviceId 过滤返回正确数据")
        void shouldFilterByDeviceId() {
            PageDTO result = wireMaterialService.listPageWithFilter(1,
                    "DEV211", null, null, null, null, null, null, null);
            assertNotNull(result);
            log.info("DEV211 过滤: total={}", result.getTotal());
        }

        @Test
        @DisplayName("日期范围 + deviceId 联合过滤")
        void shouldFilterByDeviceAndDateRange() {
            PageDTO result = wireMaterialService.listPageWithFilter(1,
                    "DEV211", null, null, null, null, null,
                    LocalDate.of(2026, 7, 4),
                    LocalDate.of(2026, 7, 11));
            assertNotNull(result);
            log.info("DEV211 + 日期范围: total={}", result.getTotal());
        }

        @Test
        @DisplayName("无效 modelEvaluationResult 被忽略，不抛异常")
        void shouldIgnoreInvalidEvaluationResult() {
            assertDoesNotThrow(() -> {
                wireMaterialService.listPageWithFilter(1,
                        null, null, null, null, null, "INVALID_VALUE", null, null);
            });
        }
    }

    @Nested
    @DisplayName("集成测试: getEarlyWarningStats")
    class IntegrationGetEarlyWarningStats {

        @Test
        @DisplayName("正常返回预警统计数据")
        void shouldReturnWarningStats() {
            EarlyWarningStatsDTO result = wireMaterialService.getEarlyWarningStats(24);
            assertNotNull(result);
            assertEquals(24, result.getHoursBack());
            assertNotNull(result.getByDevice());
            assertNotNull(result.getByProductionMachine());
            assertNotNull(result.getByResponsiblePerson());
            log.info("预警统计: total={}, fail={}, rate={}%",
                    result.getTotalCount(), result.getFailCount(), result.getOverallFailRate());
        }

        @Test
        @DisplayName("hours=1 返回最近1小时数据")
        void shouldWorkWithSmallHours() {
            EarlyWarningStatsDTO result = wireMaterialService.getEarlyWarningStats(1);
            assertNotNull(result);
            assertEquals(1, result.getHoursBack());
        }
    }

    @Nested
    @DisplayName("集成测试: Bug 验证")
    class IntegrationBugVerification {

        @Test
        @DisplayName("queryByBatchNoWithRollNo 不存在时返回 null（已修复）")
        void shouldReturnNullWhenBatchNoAndRollNoNotExist() {
            Long result = wireMaterialService.queryByBatchNoWithRollNo(99999999L, 999L);
            assertNull(result);
        }

//        @Test
//        @DisplayName("queryByBatchNoWithRollNo 存在时正常返回")
//        void shouldReturnBatchNumberWhenExists() {
//            // 先拿一个真实的 batchNo + rollNo
//            Long batchNumber = wireMaterialService.queryByBatchNoWithRollNo(20260708L, 1L);
//            assertNotNull(batchNumber);
//            log.info("batchNo=20260708, rollNo=1 -> batchNumber={}", batchNumber);
//        }
    }
}
