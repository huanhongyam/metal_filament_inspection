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
     * 主键
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
     * 生产设备
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
     * 应用场景编号
     */
    private String scenarioCode;
    /**
     * 评估详情（记录不合格的具体指标）
     */
    private String evaluationMessage;
    /**
     * 模型评估结果
     * Agent模型的评估结果，用于辅助规则引擎进行更精确的质量判断
     */
    @Builder.Default
    private EvaluationResult modelEvaluationResult = EvaluationResult.UNKNOWN;
    /**
     * Agent模型评估置信度
     * Agent模型对评估结果的置信程度，范围0-1，值越高表示模型越确信
     */
    private BigDecimal modelConfidence;
    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    /**
     * 批次
     */
    private Long batchNo;
    /**
     * 卷序
     */
    private Long rollNo;

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

}