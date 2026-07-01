package com.kunpeng.metal_filament_inspection.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WireMaterialPassRateVO {
    private BigDecimal passRate;    // 合格率
    private Long passCount;         // 合格数量
    private Long failCount;         // 不合格数量
}
