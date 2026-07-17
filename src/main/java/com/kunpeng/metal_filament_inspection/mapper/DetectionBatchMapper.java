package com.kunpeng.metal_filament_inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DetectionBatchMapper extends BaseMapper<DetectionBatch> {

    @Select("SELECT batch_number FROM detection_batch GROUP BY batch_number ORDER BY MAX(create_time) DESC")
    Page<DetectionBatch> selectBatchNumberPage(Page<DetectionBatch> page);
}
