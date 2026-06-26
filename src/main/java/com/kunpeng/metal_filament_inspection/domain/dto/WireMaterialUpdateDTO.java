package com.kunpeng.metal_filament_inspection.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class WireMaterialUpdateDTO {
    /**
     * 批次号（主键）
     */
    @TableId
    private Long batchNumber;
    /**
     * 评估详情（记录不合格的具体指标）
     */
    private String evaluationMessage;

    /**
     * Agent评估结果
     * Agent模型的评估结果，用于辅助规则引擎进行更精确的质量判断
     */
    private String modelEvaluationResult;
    /**
     * 模型评估置信度
     */
    private BigDecimal modelConfidence;
}
