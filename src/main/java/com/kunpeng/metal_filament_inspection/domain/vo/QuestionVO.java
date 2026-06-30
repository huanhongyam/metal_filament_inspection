package com.kunpeng.metal_filament_inspection.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuestionVO {

    private Long id;
    private String deviceId;
    private Long userId;
    private String questionContent;
    private String aiResponseContent;
    private Integer responseStatus;
    private LocalDateTime eventTime;
    private LocalDateTime createTime;
    private LocalDateTime responseTime;
}