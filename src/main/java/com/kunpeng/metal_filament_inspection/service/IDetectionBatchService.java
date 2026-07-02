package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.PageDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import java.util.List;

public interface IDetectionBatchService extends IService<DetectionBatch> {
    List<DetectionBatchDTO> listFlawData(Long batchNumber);

    IPage<DetectionBatchDTO> listRecentDefectData(Integer current, Integer size);

    PageDTO listPage(Integer current);
}
