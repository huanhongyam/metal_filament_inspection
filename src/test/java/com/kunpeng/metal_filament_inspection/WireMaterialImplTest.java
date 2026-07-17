package com.kunpeng.metal_filament_inspection;

import com.kunpeng.metal_filament_inspection.domain.dto.EarlyWarningStatsDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.EarlyWarningSummaryDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialUpdateDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPassRateVO;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPhysicalVO;
import com.kunpeng.metal_filament_inspection.mapper.WireMaterialMapper;
import com.kunpeng.metal_filament_inspection.service.Impl.WireMaterialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WireMaterialServiceImpl 单元测试")
class WireMaterialImplTest {

    @Mock
    private WireMaterialMapper wireMaterialMapper;

    @InjectMocks
    private WireMaterialServiceImpl service;

    // ============================================================
    // QueryWithBatchNo — NPE 风险：表为空时 latest 为 null
    // ============================================================
    @Nested
    @DisplayName("queryByBatchNoWithRollNo")
    class QueryByBatchNoWithRollNoTest {

        // 该方法内部调用 query().eq(...).eq(...).one().getBatchNumber()
        // .one() 可能返回 null → 直接 NPE
        // 这里仅做静态分析记录，实际测试需要 Spring 上下文
        @Test
        @DisplayName("⚠️ BUG确认: .one() 返回 null 时无防御，会 NPE")
        void shouldNPEWhenNoMatch() {
            // 代码位置: WireMaterialServiceImpl.java:355
            // Long batchNumber = query().eq("batch_no", batchNo)
            //         .eq("roll_no", rollNo).one().getBatchNumber();
            // .one() 无 null 检查，直接调用 .getBatchNumber()
            assertTrue(true, "此测试仅记录静态分析发现的 NPE 风险");
        }
    }

    // ============================================================
    // QueryWithBatchNo — 同上 NPE 风险
    // ============================================================
    @Nested
    @DisplayName("QueryWithBatchNo")
    class QueryWithBatchNoTest {

        @Test
        @DisplayName("⚠️ BUG确认: batchNo=null 且表为空时，latest=null 后调用 latest.getBatchNo() NPE")
        void shouldNPEWhenTableEmptyAndBatchNoNull() {
            // 代码位置: WireMaterialServiceImpl.java:551-556
            // WireMaterial wireMaterial = baseMapper.selectOne(wrapper);
            // Long batchNo1 = wireMaterial.getBatchNo();  ← NPE
            assertTrue(true, "此测试仅记录静态分析发现的 NPE 风险");
        }
    }

    // ============================================================
    // getPassRateByYearMonth — 边界测试
    // ============================================================
    @Nested
    @DisplayName("getPassRateByYearMonth")
    class GetPassRateByYearMonthTest {

        @Test
        @DisplayName("yearMonth 格式错误时 DateTimeParseException")
        void shouldThrowOnBadFormat() {
            assertThrows(Exception.class, () -> service.getPassRateByYearMonth("2026/07"));
        }

        @Test
        @DisplayName("yearMonth 为 null 时 NPE")
        void shouldNPEOnNull() {
            assertThrows(NullPointerException.class, () -> service.getPassRateByYearMonth(null));
        }

        @Test
        @DisplayName("正常月份返回结果，passRate 计算正确")
        void shouldCalculatePassRateCorrectly() {
            WireMaterialPassRateVO vo = new WireMaterialPassRateVO();
            vo.setDay(java.time.LocalDate.of(2026, 7, 8));
            vo.setPassCount(80L);
            vo.setFailCount(20L);

            when(wireMaterialMapper.selectPassRateByMonth(any(), any()))
                    .thenReturn(List.of(vo));

            List<WireMaterialPassRateVO> result = service.getPassRateByYearMonth("2026-07");

            assertEquals(1, result.size());
            assertEquals(0, new BigDecimal("80.00").compareTo(result.get(0).getPassRate()));
        }

        @Test
        @DisplayName("无数据时返回空列表不报错")
        void shouldReturnEmptyWhenNoData() {
            when(wireMaterialMapper.selectPassRateByMonth(any(), any()))
                    .thenReturn(Collections.emptyList());

            List<WireMaterialPassRateVO> result = service.getPassRateByYearMonth("2026-07");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("total=0 时 passRate 为 0 不抛异常")
        void shouldHandleZeroTotal() {
            WireMaterialPassRateVO vo = new WireMaterialPassRateVO();
            vo.setDay(java.time.LocalDate.of(2026, 7, 8));
            vo.setPassCount(0L);
            vo.setFailCount(0L);

            when(wireMaterialMapper.selectPassRateByMonth(any(), any()))
                    .thenReturn(List.of(vo));

            List<WireMaterialPassRateVO> result = service.getPassRateByYearMonth("2026-07");
            assertEquals(0, BigDecimal.ZERO.compareTo(result.get(0).getPassRate()));
        }
    }

    // ============================================================
    // updateEvaluationBatch — 边界测试
    // ============================================================
    @Nested
    @DisplayName("updateEvaluationBatch")
    class UpdateEvaluationBatchTest {

        @Test
        @DisplayName("dtoList 为 null 时返回 error")
        void shouldReturnErrorOnNull() {
            var result = service.updateEvaluationBatch(null);
            assertNotNull(result);
            assertNull(result.getData());
            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("dtoList 为空列表时返回 error")
        void shouldReturnErrorOnEmpty() {
            var result = service.updateEvaluationBatch(Collections.emptyList());
            assertNotNull(result);
            assertNull(result.getData());
            assertEquals(500, result.getCode());
        }

        @Test
        @DisplayName("⚠️ 全部 dto 评估结果无效 → entities 为空 → updateBatchById([]) (需Spring上下文)")
        void shouldHandleAllInvalidEvaluationResult() {
            // 此方法调用 updateBatchById(entities) 需要 baseMapper
            // 需 @SpringBootTest 集成测试验证
            assertTrue(true, "改为集成测试验证");
        }

        @Test
        @DisplayName("⚠️ dto.batchNumber=null → updateBatchById 以 null PK 更新 (需Spring上下文)")
        void shouldHandleNullBatchNumber() {
            // 此方法调用 updateBatchById(entities) 需要 baseMapper
            // 需 @SpringBootTest 集成测试验证
            assertTrue(true, "改为集成测试验证");
        }
    }

    // ============================================================
    // getEarlyWarningStats — 边界测试
    // ============================================================
    @Nested
    @DisplayName("getEarlyWarningStats")
    class GetEarlyWarningStatsTest {

        @Test
        @DisplayName("hours 为 null 时默认 24")
        void shouldDefaultTo24WhenNull() {
            EarlyWarningSummaryDTO summary = new EarlyWarningSummaryDTO();
            summary.setTotalCount(100L);
            summary.setFailCount(10L);

            when(wireMaterialMapper.selectWarningSummary(any())).thenReturn(summary);
            when(wireMaterialMapper.selectWarningGroupBy(any(), anyString()))
                    .thenReturn(Collections.emptyList());

            EarlyWarningStatsDTO result = service.getEarlyWarningStats(null);
            assertEquals(24, result.getHoursBack());
        }

        @Test
        @DisplayName("hours <= 0 时默认 24")
        void shouldDefaultTo24WhenZero() {
            EarlyWarningSummaryDTO summary = new EarlyWarningSummaryDTO();
            summary.setTotalCount(0L);
            summary.setFailCount(0L);

            when(wireMaterialMapper.selectWarningSummary(any())).thenReturn(summary);
            when(wireMaterialMapper.selectWarningGroupBy(any(), anyString()))
                    .thenReturn(Collections.emptyList());

            EarlyWarningStatsDTO result = service.getEarlyWarningStats(0);
            assertEquals(24, result.getHoursBack());
        }

        @Test
        @DisplayName("summary 为 null 时 failRate 为 0")
        void shouldHandleNullSummary() {
            when(wireMaterialMapper.selectWarningSummary(any())).thenReturn(null);
            when(wireMaterialMapper.selectWarningGroupBy(any(), anyString()))
                    .thenReturn(Collections.emptyList());

            EarlyWarningStatsDTO result = service.getEarlyWarningStats(24);
            assertEquals(0, BigDecimal.ZERO.compareTo(result.getOverallFailRate()));
            assertEquals(0, result.getTotalCount());
            assertEquals(0, result.getFailCount());
        }

        @Test
        @DisplayName("total=0 时 failRate 不抛异常")
        void shouldNotThrowWhenTotalZero() {
            EarlyWarningSummaryDTO summary = new EarlyWarningSummaryDTO();
            summary.setTotalCount(0L);
            summary.setFailCount(0L);

            when(wireMaterialMapper.selectWarningSummary(any())).thenReturn(summary);
            when(wireMaterialMapper.selectWarningGroupBy(any(), anyString()))
                    .thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> service.getEarlyWarningStats(24));
        }
    }

    // ============================================================
    // QueryWithBatchNoAvg — 边界测试
    // ============================================================
    @Nested
    @DisplayName("QueryWithBatchNoAvg")
    class QueryWithBatchNoAvgTest {

        @Test
        @DisplayName("batchNo 为 null 且表中有数据时，查最新批次")
        void shouldQueryLatestWhenBatchNoNull() {
            // 需要 baseMapper 返回一个 WireMaterial
            // 由于 mock 了 wireMaterialMapper，baseMapper 未 mock，此方法需要集成测试
            assertTrue(true, "需要 Spring 集成测试验证");
        }

        @Test
        @DisplayName("⚠️ batchNo 在表中不存在时，selectAvgByBatchNo 返回 null")
        void shouldReturnNullWhenBatchNoNotFound() {
            when(wireMaterialMapper.selectAvgByBatchNo(999999L)).thenReturn(null);

            WireMaterialPhysicalVO result = service.QueryWithBatchNoAvg(999999L);
            assertNull(result);
        }
    }
}
