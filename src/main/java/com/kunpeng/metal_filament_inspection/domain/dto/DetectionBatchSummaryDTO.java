package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetectionBatchSummaryDTO {
    /**
     * 批号
     */
    private Long batchNumber;
    /**
     * 总图片数（取第一条）
     */
    private Integer totalImages;
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
     *  示例图片URL（取第一条）
     */
    private String imgUrl;
}