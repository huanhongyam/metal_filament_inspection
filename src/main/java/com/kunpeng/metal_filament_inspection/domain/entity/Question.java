package com.kunpeng.metal_filament_inspection.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问题实体类 - 存储设备问题数据
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    

    private Long id;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 用户问题内容
     */
    private String questionContent;
    
    /**
     * 响应状态 (0: 未处理, 1: 已处理)
     */
    @Builder.Default
    private Integer responseStatus = 0;
    
    /**
     * AI响应内容
     */
    private String aiResponseContent;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 响应时间
     */
    private LocalDateTime responseTime;
} 