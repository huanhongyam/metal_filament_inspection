package com.kunpeng.metal_filament_inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPassRateVO;
import com.kunpeng.metal_filament_inspection.domain.vo.WireMaterialPhysicalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WireMaterialMapper extends BaseMapper<WireMaterial> {

    /**
     * 按批次号查询物理参数平均值（AVG聚合）
     */
    @Select("SELECT batch_no, " +
            "CAST(AVG(diameter) AS DECIMAL(10,2)) as diameter, " +
            "CAST(AVG(resistance) AS DECIMAL(10,2)) as resistance, " +
            "CAST(AVG(extensibility) AS DECIMAL(10,2)) as extensibility, " +
            "CAST(AVG(weight) AS DECIMAL(10,2)) as weight, " +
            "MAX(scenario_code) as scenario_code " +
            "FROM wire_material WHERE batch_no = #{batchNo} GROUP BY batch_no")
    WireMaterialPhysicalVO selectAvgByBatchNo(@Param("batchNo") Long batchNo);

    /**
     * 按月按天分组统计合格/不合格数量
     */
    @Select("SELECT DATE(create_time) as day, " +
            "SUM(CASE WHEN model_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
            "SUM(CASE WHEN model_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount " +
            "FROM wire_material " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY DATE(create_time) ORDER BY day")
    List<WireMaterialPassRateVO> selectPassRateByMonth(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    /**
     * 分页查询批次平均值列表（消除 N+1）
     */
    @Select("SELECT batch_no, " +
            "CAST(AVG(diameter) AS DECIMAL(10,2)) as diameter, " +
            "CAST(AVG(resistance) AS DECIMAL(10,2)) as resistance, " +
            "CAST(AVG(extensibility) AS DECIMAL(10,2)) as extensibility, " +
            "CAST(AVG(weight) AS DECIMAL(10,2)) as weight, " +
            "MAX(scenario_code) as scenario_code " +
            "FROM wire_material GROUP BY batch_no ORDER BY MAX(create_time) DESC")
    IPage<WireMaterialPhysicalVO> selectBatchAvgPage(IPage<WireMaterialPhysicalVO> page);
}
