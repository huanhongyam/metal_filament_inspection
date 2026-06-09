package com.kunpeng.metal_filament_inspection.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * 设备实体类 - 存储设备状态信息
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        ON("ON"),
        OFF("OFF");
        
        private final String value;
        
        DeviceStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    /**
     * 设备ID - 主键
     */

    private String deviceId;
    
    /**
     * 设备代码（用于匹配批次号中的机器号13-14位）
     */
    private String deviceCode;
    
    /**
     * 设备状态 (ON/OFF)
     */

    @Builder.Default
    private DeviceStatus status = DeviceStatus.OFF;
    
    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
    
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }
    
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
} 