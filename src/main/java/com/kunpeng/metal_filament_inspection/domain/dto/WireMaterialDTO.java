package com.kunpeng.metal_filament_inspection.domain.dto;

import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线材分页响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WireMaterialDTO {
    /**
     * 批次卷序
     */
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
     * 应用场景编号
     */
    private String scenarioCode;

    /**
     * 设备代码
     */
    private String deviceCode;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 规则引擎评估结果
     */
    private WireMaterial.EvaluationResult evaluationResult;

    /**
     * 评估详情
     */
    private String evaluationMessage;

    /**
     * 模型评估结果
     */
    private WireMaterial.EvaluationResult modelEvaluationResult;

    /**
     * 模型评估置信度
     */
    private BigDecimal modelConfidence;

    /**
     * 最终评估结果
     */
    private WireMaterial.FinalEvaluationResult finalEvaluationResult;
    /**
     * 批次
     */
    private Long batchNo;
    /**
     * 卷次
     */
    private Long rollNo;

} 