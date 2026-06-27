package com.kunpeng.metal_filament_inspection.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DetectTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 硬件端上报的两个时间字段
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // IdWoker生成
    private Long batchNumber;
}