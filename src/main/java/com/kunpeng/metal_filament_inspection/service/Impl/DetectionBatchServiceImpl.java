package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.DetectionBatchDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import com.kunpeng.metal_filament_inspection.mapper.DetectionBatchMapper;
import com.kunpeng.metal_filament_inspection.service.IDetectionBatchService;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class DetectionBatchServiceImpl extends ServiceImpl<DetectionBatchMapper, DetectionBatch> implements IDetectionBatchService {
    @Override
    public List<DetectionBatchDTO> listFlawData(Long batchNumber) {
        log.info("查询线材表面信息，批次号：{}", batchNumber);
        List<DetectionBatch> detectionBatch = query().eq("batch_number", batchNumber).list();
        List<DetectionBatchDTO> detectionBatchDTOS = detectionBatch.stream()
                .map(item -> BeanUtil.copyProperties(item, DetectionBatchDTO.class))
                .filter(dto -> !SystemConstants.DEFAULT_QINIU_URL.equals(dto.getImgUrl()))
                .collect(Collectors.toList());
        return detectionBatchDTOS;
    }

    @Override
    public IPage<DetectionBatchDTO> listRecentDefectData(Integer current, Integer size) {
        // 1. 创建分页对象
        Page<DetectionBatch> page = new Page<>(current, size);
        // 2. 计算时间范围（最近7天）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        // 3. 查有缺陷的记录
        LambdaQueryWrapper<DetectionBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(DetectionBatch::getCreateTime, sevenDaysAgo, now)
                .and(qw -> qw
                        .ne(DetectionBatch::getImgUrl, SystemConstants.DEFAULT_QINIU_URL)
                )
                .orderByDesc(DetectionBatch::getCreateTime);
        // 4. 执行分页查询（
        Page<DetectionBatch> pageResult = page(page,wrapper);
        // 5. 转换 DTO
        return pageResult.convert(item -> BeanUtil.copyProperties(item, DetectionBatchDTO.class));
    }

}
