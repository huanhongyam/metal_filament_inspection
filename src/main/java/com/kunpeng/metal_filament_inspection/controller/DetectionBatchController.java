package com.kunpeng.metal_filament_inspection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.service.IDetectionBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "表面缺陷检测记录控制接口")
@Slf4j
@RestController
@RequestMapping("/api/detection-batch")
public class DetectionBatchController {
    @Autowired
    private IDetectionBatchService detectionBatchService;

    @Operation(summary = "根据批次号查询线材表面缺陷数据")
    @GetMapping("/info/{batchNumber}")
    public Result<List<DetectionBatchDTO>> getWireMaterialByBatchNumber(
            @PathVariable Long batchNumber) {
        return Result.success(detectionBatchService.listFlawData(batchNumber));
    }
    @Operation(summary = "分页查询最近7天内有缺陷的线材表面缺陷数据")
    @GetMapping("/recent")
    public Result<IPage<DetectionBatchDTO>> getRecentDefectData(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10",required = false) Integer size) {
        return Result.success(detectionBatchService.listRecentDefectData(current, size));
    }
}
