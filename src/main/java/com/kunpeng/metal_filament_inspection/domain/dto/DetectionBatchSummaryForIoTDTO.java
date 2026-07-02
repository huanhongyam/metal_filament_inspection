package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetectionBatchSummaryForIoTDTO {
    /**
     *  平均置信度（取第一条）
     */
    private BigDecimal avgConfidence;
    /**
     *  划痕总数（累加）
     */
    private Integer scratchCount;
    /**
     *  块状缺陷总数（累加）
     */
    private Integer blockDefectCount;
    /**
     *  簇状缺陷总数（累加）
     */
    private Integer clusterDefectCount;
    /**
     *  金属毛刺总数（累加）
     */
    private Integer metalBurrCount;
    /**
     * 磨损总数（累加）
     */
    private Integer scuffCount;
    /**
     * Agent评估状态
     */
    private String modelEvaluationResult;
}