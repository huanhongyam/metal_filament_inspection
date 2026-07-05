package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
     * 缺陷图片URL列表（只存有缺陷的七牛云图片，默认图不存）
     */
    private List<String> imgUrls;
}