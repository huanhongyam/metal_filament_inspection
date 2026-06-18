package com.kunpeng.metal_filament_inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WireMaterialMapper  extends BaseMapper<WireMaterial> {
}
