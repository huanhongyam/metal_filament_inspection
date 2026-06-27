package com.kunpeng.metal_filament_inspection.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 缺陷检测批次记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@TableName("detection_batch")
public class DetectionBatch {

    /**
     * 主键ID（IdWoker）
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 检测记录ID
     */
    private Long batchNumber;

    /**
     * 检测起始时间（用于界定图片归属范围）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 检测结束时间（用于界定图片归属范围）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 本次批次处理的图片总数
     */
    private Integer totalImages;

    /**
     * 划痕数量
     */
    private Integer scratchCount;

    /**
     * 块状缺陷数量
     */
    private Integer blockDefectCount;

    /**
     * 簇状缺陷数量
     */
    private Integer clusterDefectCount;

    /**
     * 金属毛刺数量
     */
    private Integer metalBurrCount;

    /**
     * 擦伤数量
     */
    private Integer scuffCount;

    /**
     * 总体平均置信度
     */
    private BigDecimal avgConfidence;

    /**
     * 状态：PENDING / PROCESSING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 失败时的错误信息
     */
    private String errorMsg;

    /**
     * 七牛云图片URL
     */
    private String imgUrl;

    /**
     * 系统创建时间（数据库自动生成）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}