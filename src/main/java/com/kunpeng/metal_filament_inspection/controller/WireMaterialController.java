package com.kunpeng.metal_filament_inspection.controller;

import cn.hutool.core.bean.BeanUtil;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "线材管理控制器")
@Slf4j
@RestController
@RequestMapping("/api/wire-material")
public class WireMaterialController {
    @Autowired
    private IWireMaterialService wireMaterialService;

    /**
     * 分页查询线材列表
     * 权限：已认证用户
     */
    @Operation(summary = "分页查询线材列表")
    @GetMapping("/list")
    public Result<List<WireMaterialDTO>> getWireMaterialList(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return wireMaterialService.listPage(current);
    }
    /**
     * 根据批次号查询线材信息
     * 权限：无需认证（公开接口）
     */
    @Operation(summary = "根据批次号查询线材")
    @GetMapping("/info/{batchNumber}")
    public Result<WireMaterialDTO> getWireMaterialByBatchNumber(
            @PathVariable @NotNull(message = "批次号不能为空") Long batchNumber) {
        log.info("查询线材信息，批次号：{}", batchNumber);
        WireMaterial wireMaterial = wireMaterialService.query().eq("batch_number", batchNumber).one();
        return Result.success(BeanUtil.copyProperties(wireMaterial, WireMaterialDTO.class));
    }
    /**
     * 更新线材信息
     * 权限：管理员（roleId=1）
     */
    @Operation(summary = "更新线材信息")
    @PutMapping("/{batchNumber}")
    public Result<Boolean> updateWireMaterial(
            @PathVariable @NotBlank(message = "批次号不能为空") Long batchNumber,
            @Valid @RequestBody WireMaterialDTO wireMaterialDTO) {
        return wireMaterialService.updateByBatchNumber(wireMaterialDTO,batchNumber);
    }
    /**
     * 删除线材记录
     * 权限：管理员（roleId=1）
     */
    @Operation(summary = "删除线材记录")
    @DeleteMapping("/{batchNumber}")
    public Result<Boolean> deleteWireMaterial(@PathVariable @NotBlank(message = "批次号不能为空") Long batchNumber) {
        return wireMaterialService.deleteById(batchNumber);
    }

}
