package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchSummaryDTO;
import java.util.*;

/**
 * 检测数据聚合工具类
 */
public class DetectionSummary {

    /**
     * 按批号聚合检测数据
     * @param list 原始检测数据列表
     * @return 按批号聚合后的汇总列表
     */
    public static List<DetectionBatchSummaryDTO> summarizeByBatchLoop(List<DetectionBatchDTO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, DetectionBatchSummaryDTO> map = new HashMap<>();

        for (DetectionBatchDTO dto : list) {
            Long batchNumber = dto.getBatchNumber();
            DetectionBatchSummaryDTO summary = map.get(batchNumber);
            if (summary == null) {
                summary = new DetectionBatchSummaryDTO();
                summary.setBatchNumber(batchNumber);
                summary.setTotalImages(dto.getTotalImages());          // 总图片数（取第一条）
                summary.setAvgConfidence(dto.getAvgConfidence());      // 平均置信度（取第一条）
                summary.setImgUrl(dto.getImgUrl());                    // 示例图片URL（取第一条）
                summary.setScratchCount(dto.getScratchCount());        // 划痕总数（累加）
                summary.setBlockDefectCount(dto.getBlockDefectCount()); // 块状缺陷总数（累加）
                summary.setClusterDefectCount(dto.getClusterDefectCount()); // 簇状缺陷总数（累加）
                summary.setMetalBurrCount(dto.getMetalBurrCount());    // 金属毛刺总数（累加）
                summary.setScuffCount(dto.getScuffCount());            // 磨损总数（累加）
                map.put(batchNumber, summary);
            } else {
                // 累加缺陷计数
                summary.setScratchCount(summary.getScratchCount() + dto.getScratchCount());
                summary.setBlockDefectCount(summary.getBlockDefectCount() + dto.getBlockDefectCount());
                summary.setClusterDefectCount(summary.getClusterDefectCount() + dto.getClusterDefectCount());
                summary.setMetalBurrCount(summary.getMetalBurrCount() + dto.getMetalBurrCount());
                summary.setScuffCount(summary.getScuffCount() + dto.getScuffCount());
            }
        }

        return new ArrayList<>(map.values());
    }
}