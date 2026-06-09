package com.kunpeng.metal_filament_inspection.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用场景实体类 - 存储线材应用场景的标准规范
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationScenario {
    
    /**
     * 应用场景编号 - 主键（固定两位数字字符串）
     */
    @Id
    private String scenarioCode;
    
    /**
     * 应用场景名称
     */
    private String scenarioName;
    
    /**
     * 线材类型（Cu、Al、Ni、Ti、Zn）
     */
    private String wireType;
    
    /**
     * 电导率标准下限
     */
    private BigDecimal conductivityMin;
    
    /**
     * 电导率标准上限
     */
    private BigDecimal conductivityMax;
    
    /**
     * 延展率标准下限（%）
     */
    private BigDecimal extensibilityMin;
    
    /**
     * 延展率标准上限（%）
     */
    private BigDecimal extensibilityMax;
    
    /**
     * 重量标准下限（g）
     */
    private BigDecimal weightMin;
    
    /**
     * 重量标准上限（g）
     */
    private BigDecimal weightMax;
    
    /**
     * 直径标准下限（mm）
     */
    private BigDecimal diameterMin;
    
    /**
     * 直径标准上限（mm）
     */
    private BigDecimal diameterMax;
    
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
} 