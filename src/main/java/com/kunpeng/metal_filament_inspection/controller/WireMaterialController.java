package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.dto.WireMaterialDTO;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
