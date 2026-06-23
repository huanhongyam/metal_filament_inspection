package com.kunpeng.metal_filament_inspection.domain.dto;

import com.kunpeng.metal_filament_inspection.domain.entity.Device;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备代码
     */
    private String deviceCode;

    /**
     * 设备状态
     */
    private Device.DeviceStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}