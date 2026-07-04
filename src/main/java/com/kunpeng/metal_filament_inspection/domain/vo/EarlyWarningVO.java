package com.kunpeng.metal_filament_inspection.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EarlyWarningVO {
    private List<WarningItem> topManufacturers;
    private List<WarningItem> topResponsiblePersons;
    private List<WarningItem> topDevices;
    private String aiAnalysis;
    private LocalDateTime analysisTime;

    @Data
    public static class WarningItem {
        private String name;
        private Long totalCount;
        private Long failCount;
        private BigDecimal failRate;
    }
}
