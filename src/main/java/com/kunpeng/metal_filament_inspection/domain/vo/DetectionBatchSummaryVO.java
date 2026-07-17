package com.kunpeng.metal_filament_inspection.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DetectionBatchSummaryVO {
    private Long batchNumber;
    private BigDecimal avgConfidence;
    private Integer scratchCount;
    private Integer blockDefectCount;
    private Integer clusterDefectCount;
    private Integer metalBurrCount;
    private Integer scuffCount;
    private String modelEvaluationResult;
    private List<String> imgUrls;
}