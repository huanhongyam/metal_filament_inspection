package com.kunpeng.metal_filament_inspection.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import com.kunpeng.metal_filament_inspection.mapper.DetectionBatchMapper;
import com.kunpeng.metal_filament_inspection.service.IDetectionBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class DetectionBatchImpl extends ServiceImpl<DetectionBatchMapper, DetectionBatch> implements IDetectionBatchService {
}
