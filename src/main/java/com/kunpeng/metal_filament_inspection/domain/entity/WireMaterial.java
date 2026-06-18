package com.kunpeng.metal_filament_inspection.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线材实体类 - 存储金属微丝检测数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("wire_material")
public class WireMaterial implements Serializable {
    
    /**
     * 批次卷序 - 主键
     */
    @TableId(type = IdType.INPUT)
    private Long batchNumber;
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 金属丝直径
     */
    private BigDecimal diameter;
    
    /**
     * 电导率
     */
    private BigDecimal resistance;
    
    /**
     * 延展率
     */
    private BigDecimal extensibility;
    
    /**
     * 重量
     */
    private BigDecimal weight;
    
    /**
     * 原始生产信息（十六进制GBK编码）
     */
    private String sourceOriginRaw;
    
    /**
     * 生产商
     */
    private String manufacturer;
    
    /**
     * 负责人
     */
    private String responsiblePerson;
    
    /**
     * 工艺类型
     */
    private String processType;
    
    /**
     * 生产机器
     */
    private String productionMachine;
    
    /**
     * 联系方式（邮箱）
     */
    private String contactEmail;
    
    /**
     * 应用场景编号（从批次号解析得出）
     */
    private String scenarioCode;
    
    /**
     * 设备代码（从批次号解析得出，对应批次号13-14位）
     */
    private String deviceCode;
    
    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 规则引擎评估结果
     */
    @Builder.Default
    private EvaluationResult evaluationResult = EvaluationResult.UNKNOWN;
    
    /**
     * 评估详情（记录不合格的具体指标）
     */
    private String evaluationMessage;
    
    /**
     * 模型评估结果
     * 机器学习模型的评估结果，用于辅助规则引擎进行更精确的质量判断
     */
    @Builder.Default
    private EvaluationResult modelEvaluationResult = EvaluationResult.UNKNOWN;
    
    /**
     * 模型评估置信度
     * 模型对评估结果的置信程度，范围0-1，值越高表示模型越确信
     */
    private BigDecimal modelConfidence;
    
    /**
     * 最终评估结果
     * 综合规则引擎和模型评估结果的最终质量判断，考虑人工审核需求
     */
    @Builder.Default
    private FinalEvaluationResult finalEvaluationResult = FinalEvaluationResult.UNKNOWN;
    
    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 评估结果枚举
     */
    public enum EvaluationResult {
        PASS("合格"),
        FAIL("不合格"), 
        UNKNOWN("未评估");
        
        private final String description;
        
        EvaluationResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 最终评估结果枚举
     * 包含需要人工审核的状态
     */
    public enum FinalEvaluationResult {
        PASS("合格"),
        FAIL("不合格"), 
        PENDING_REVIEW("待人工审核"),
        UNKNOWN("未评估");
        
        private final String description;
        
        FinalEvaluationResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 