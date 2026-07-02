package com.kunpeng.metal_filament_inspection.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WireInfoWithDetectionInfo {
    /**
     * 批次号（主键）
     */
    @TableId
    private Long batchNumber;
    /**
     * 平均置信度
     */
    private BigDecimal avgConfidence;
    /**
     * 缺陷数量
     */
    private Integer scratchCount;
    private Integer blockDefectCount;
    private Integer clusterDefectCount;
    private Integer metalBurrCount;
    private Integer scuffCount;
    /**
     * 评估结果
     */
    private String modelEvaluationResult;
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
    @TableField("process_type")
    private String processType;

    /**
     * 应用场景编号
     */
    private String scenarioCode;


    /**
     * 批次
     */
    private Long batchNo;

    /**
     * 卷序
     */
    private Long rollNo;

    /**
     * 模型置信度
     */
    private BigDecimal modelConfidence;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}
