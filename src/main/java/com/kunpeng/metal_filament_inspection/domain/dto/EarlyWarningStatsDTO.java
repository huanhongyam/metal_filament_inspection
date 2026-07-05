package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EarlyWarningStatsDTO {
    private int hoursBack;
    private int totalCount;
    private int failCount;
    private BigDecimal overallFailRate;
    private List<GroupStats> byProductionMachine;
    private List<GroupStats> byResponsiblePerson;
    private List<GroupStats> byDevice;

    @Data
    public static class GroupStats {
        private String name;
        private Long totalCount;
        private Long failCount;
        private BigDecimal failRate;
    }
}
