package com.kunpeng.metal_filament_inspection.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.*;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.domain.vo.*;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "线材管理控制器")
@Slf4j
@RestController
@RequestMapping("/api/wire-material")
public class WireMaterialController {
    @Autowired
    private IWireMaterialService wireMaterialService;

    /**
     * 条件查询线材列表
     * 权限：已认证用户
     */
    @Operation(summary = "条件查询线材列表")
    @PostMapping("/list-agent")
    public Result<List<WireMaterialDTO>> getWireMaterialListQuery(@RequestParam(value = "limit", defaultValue = "10" ,required = false) Integer limit,
                                                                  @RequestBody(required = false) WireMaterialQueryDTO wireMaterialDTO
    ) {
        return Result.success(wireMaterialService.listQueryPage(limit,wireMaterialDTO));
    }
    /**
     * 根据批次查询平均数据
     * 权限：已认证用户
     */
    @Operation(summary = "根据批次查询平均数据")
    @GetMapping("/batchNoAvg")
    public Result<WireMaterialPhysicalVO> QueryWithBatchNoAvg(@RequestParam(required = false) Long batchNo) {
        return Result.success(wireMaterialService.QueryWithBatchNoAvg(batchNo));
    }
    /**
     * 分页查询最近记录平均数据
     * 权限：已认证用户
     */
    @Operation(summary = "分页查询最近记录平均数据")
    @GetMapping("/list-batchNoAvg")
    public Result<Page<WireMaterialPhysicalVO>> QueryListWithBatchNoAvg(@RequestParam(value = "current", defaultValue = "1") Integer current) throws JsonProcessingException {
        return wireMaterialService.QueryListWithBatchNoAvg(current);
    }
    /**
     * 根据批次查询数据
     * 权限：已认证用户
     */
    @Operation(summary = "根据批次查询数据")
    @GetMapping("/list-batchNo")
    public Result<List<WireMaterialPhysicalVO>> QueryWithBatchNo(@RequestParam(required = false) Long batchNo) {
        return Result.success(wireMaterialService.QueryWithBatchNo(batchNo));
    }
    /**
     * 分页查询线材列表
     * 权限：已认证用户
     */
    @Operation(summary = "分页查询线材列表")
    @GetMapping("/list")
    public Result<PageDTO> getWireMaterialList(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.success(wireMaterialService.listPage(current));
    }

    @Operation(summary = "条件分页查询线材列表")
    @GetMapping("/page")
    public Result<PageDTO> getWireMaterialPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "scenarioCode", required = false) String scenarioCode,
            @RequestParam(value = "batchNo", required = false) Long batchNo,
            @RequestParam(value = "productionMachine", required = false) String productionMachine,
            @RequestParam(value = "responsiblePerson", required = false) String responsiblePerson,
            @RequestParam(value = "modelEvaluationResult", required = false) String modelEvaluationResult,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(wireMaterialService.listPageWithFilter(current, deviceId, scenarioCode,
                batchNo, productionMachine, responsiblePerson, modelEvaluationResult, startDate, endDate));
    }
    /**
     * 查询线材条数
     * 权限：已认证用户
     */
    @Operation(summary = "查询线材条数")
    @GetMapping("/count")
    public Result<Long> getWireMaterialCount() {
        return Result.success(wireMaterialService.count());
    }
    /**
     * 查询线材合格率,合格数量，不合格数量
     * 权限：已认证用户
     */
    @Operation(summary = "查询线材合格率,合格数量，不合格数量")
    @GetMapping("/pass-param")
    public Result<List<WireMaterialPassRateVO>> getWireMaterialPassParam(@RequestParam String yearMonth) {
        return Result.success(wireMaterialService.getPassRateByYearMonth(yearMonth));
    }
    @Operation(summary = "根据批次卷序查询线材表面缺陷数据")
    @GetMapping("/info")
    public Result<Long> getWireMaterialByBatchNumber(
            @RequestParam Long batchNo,
            @RequestParam Long rollNo
    ) {
        return Result.success(wireMaterialService.queryByBatchNoWithRollNo(batchNo,rollNo));
    }

    @Operation(summary = "根据批次卷序查询线材信息,缺陷聚合数据")
    @GetMapping("/info-with-detection")
    public Result<WireInfoWithDetectionInfo> getWireInfoWithDetection(
            @RequestParam Long batchNo,
            @RequestParam Long rollNo) {
        WireDTO wireDTO = new WireDTO(batchNo,rollNo);
        return wireMaterialService.queryWireMaterialByBatchNoWithRollNo(wireDTO);
    }
    /**
     * 根据批次号查询线材信息
     * 权限：无需认证（公开接口）
     */
    @Operation(summary = "根据批次号查询线材")
    @GetMapping("/info/{batchNumber}")
    public Result<WireMaterialDTO> getWireMaterialByBatchNumber(
            @PathVariable  Long batchNumber) {
        log.info("查询线材信息，批次号：{}", batchNumber);
        WireMaterial wireMaterial = wireMaterialService.query().eq("batch_number", batchNumber).one();
        return Result.success(BeanUtil.copyProperties(wireMaterial, WireMaterialDTO.class));
    }
    /**
     * 更新线材信息
     * 权限：管理员（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "更新线材信息")
    @PutMapping("/{batchNumber}")
    public Result<Boolean> updateWireMaterial(
            @PathVariable  Long batchNumber,
            @Valid @RequestBody WireMaterialDTO wireMaterialDTO) {
        return wireMaterialService.updateByBatchNumber(wireMaterialDTO,batchNumber);
    }
    /**
     * 删除线材记录
     * 权限：管理员（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "删除线材记录")
    @DeleteMapping("/{batchNumber}")
    public Result<Boolean> deleteWireMaterial(@PathVariable Long batchNumber) {
        return wireMaterialService.deleteById(batchNumber);
    }
    /**
     * 增加线材记录
     * 权限：无需认证（公开接口）
     */
    @Operation(summary = "增加线材记录")
    @PostMapping
    public Result<Boolean> createDevice(@RequestBody WireMaterialSaveDTO wireMaterialSaveDTO) {
        return wireMaterialService.saveWireMaterial(wireMaterialSaveDTO);
    }
    /**
     * 根据批次卷序查询线材是否存在
     * 权限：无需认证（公开接口）
     */
    @Operation(summary = "根据批次卷序查询线材是否存在")
    @GetMapping("/check")
    public Result<Boolean> checkWireMaterialByBatchNoWithRollNo(
            @RequestParam  Long batchNo,@RequestParam Long rollNo) {
        return wireMaterialService.checkByBatchNoWithRollNo(batchNo,rollNo);
    }
    /**
     * Agent 查询未评估线材
     * 用于 Python 定时任务拉取最近 N 小时内 model_evaluation_result = UNKNOWN 的记录
     * 权限：Agent 专用（pass4agent）
     */
    @Operation(summary = "Agent查询未评估线材")
    @GetMapping("/unevaluated")
    public Result<List<WireMaterialDTO>> getUnevaluatedWireMaterials(
            @RequestParam(value = "hours", defaultValue = "24") Integer hours,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        log.info("Agent 查询未评估线材，回溯{}小时，最多{}条", hours, limit);
        return Result.success(wireMaterialService.listUnevaluated(hours, limit));
    }
    /**
     * Agent 更新线材评估结果
     * 不走 @RequireAdmin，使用 pass4agent 头鉴权
     * 权限：Agent 专用
     */
   @Operation(summary = "Agent更新评估结果")
   @PutMapping("/{batchNumber}/evaluation")
   public Result<Boolean> updateEvaluation(
           @PathVariable Long batchNumber,
   @Valid @RequestBody WireMaterialUpdateDTO dto) {
       log.info("Agent 回写评估结果 — 批次号：{}", batchNumber);
       return wireMaterialService.updateEvaluation(batchNumber, dto);
   }
    @Operation(summary = "Agent批量更新评估结果")
    @PutMapping("/evaluation-batch")
    public Result<Integer> updateEvaluationBatch(
            @Valid @RequestBody List<WireMaterialUpdateDTO> dtoList) {
        log.info("Agent 批量回写评估结果，共 {} 条", dtoList.size());
        return wireMaterialService.updateEvaluationBatch(dtoList);
    }

    /**
     * 主动触发单条线材评估
     * 权限：管理员（roleId=1）
     */
    @Operation(summary = "主动触发单条线材评估")
    @PostMapping("/{batchNumber}/trigger-evaluation")
    public Result<WireMaterialUpdateDTO> triggerEvaluation(@PathVariable Long batchNumber) {
        log.info("管理员触发单条评估 — 批次号：{}", batchNumber);
        return wireMaterialService.triggerEvaluation(batchNumber);
    }

    /**
     * 预警分析 — Agent 专用
     * 返回按生产设备/负责人/设备分组的不合格统计数据
     */
    @Operation(summary = "Agent 获取预警统计数据")
    @GetMapping("/early-warning-stats")
    public Result<EarlyWarningStatsDTO> getEarlyWarningStats(
            @RequestParam(value = "hours", defaultValue = "24") Integer hours) {
        return Result.success(wireMaterialService.getEarlyWarningStats(hours));
    }

    /**
     * 预警分析 — AI 分析
     * 权限：管理员（roleId=1）
     */
    @Operation(summary = "触发 AI 预警分析")
    @PostMapping("/early-warning")
    public Result<EarlyWarningVO> triggerEarlyWarning(
            @RequestParam(value = "hours", defaultValue = "24") Integer hours,
            @RequestParam Integer status) throws JsonProcessingException {
        return wireMaterialService.triggerEarlyWarning(hours,status);
    }

}
