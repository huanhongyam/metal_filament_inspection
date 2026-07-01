package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.Device;
import com.kunpeng.metal_filament_inspection.service.IDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "设备接口")
@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceController {
    @Autowired
    private IDeviceService deviceService;

    /**
     * 分页查询设备列表
     * 权限：已认证用户
     */
    @Operation(summary = "分页查询")
    @GetMapping("/list")
    public Result<PageDTO> getDeviceList(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.success(deviceService.listPage(current));
    }
    /**
     * 查询设备启动数
     * 权限：已认证用户
     */
    @Operation(summary = "查询设备启动数")
    @GetMapping("/start")
    public Result<Map<Long,Long>> getDeviceStart() {
        return Result.success(deviceService.listStart());
    }
    /**
     * 根据ID删除设备
     * 权限：仅管理员用户（roleId=1）
     */
    @RequireAdmin
    @Operation(summary = "根据ID删除设备")
    @DeleteMapping("/{deviceId}")
    public Result<Boolean> deleteDeviceById(@PathVariable Long deviceId) {
        return deviceService.removeDeviceById(deviceId);
    }
    /**
     * 根据设备ID查询设备信息
     * 权限：已认证用户
     */
    @Operation(summary = "根据ID查找设备")
    @GetMapping("/{deviceId}")
    public Result<Device> queryDeviceById(@PathVariable Long deviceId) {
        return Result.success(deviceService.getById(deviceId));
    }
    @Operation(summary = "创建设备")
    @PostMapping
    public Result<Boolean> createDevice(@RequestBody Device device) {
        return deviceService.saveDevice(device);
    }

}
