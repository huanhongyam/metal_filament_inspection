package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Component
public class WireMaterialStats {
    private BigDecimal sumDiameter = BigDecimal.ZERO;
    private BigDecimal sumResistance = BigDecimal.ZERO;
    private BigDecimal sumExtensibility = BigDecimal.ZERO;
    private BigDecimal sumWeight = BigDecimal.ZERO;
    private long countDiameter = 0;
    private long countResistance = 0;
    private long countExtensibility = 0;
    private long countWeight = 0;

    // 累加一个 WireMaterial 对象（跳过 null 字段）
    public void accept(WireMaterial item) {
        if (item.getDiameter() != null) {
            sumDiameter = sumDiameter.add(item.getDiameter());
            countDiameter++;
        }
        if (item.getResistance() != null) {
            sumResistance = sumResistance.add(item.getResistance());
            countResistance++;
        }
        if (item.getExtensibility() != null) {
            sumExtensibility = sumExtensibility.add(item.getExtensibility());
            countExtensibility++;
        }
        if (item.getWeight() != null) {
            sumWeight = sumWeight.add(item.getWeight());
            countWeight++;
        }
    }

    // 获取平均值（保留2位小数，四舍五入，若无数据则返回 null）
    public BigDecimal getAvgDiameter() {
        return countDiameter == 0 ? null : sumDiameter.divide(BigDecimal.valueOf(countDiameter), 2, RoundingMode.HALF_UP);
    }
    public BigDecimal getAvgResistance() {
        return countResistance == 0 ? null : sumResistance.divide(BigDecimal.valueOf(countResistance), 2, RoundingMode.HALF_UP);
    }
    public BigDecimal getAvgExtensibility() {
        return countExtensibility == 0 ? null : sumExtensibility.divide(BigDecimal.valueOf(countExtensibility), 2, RoundingMode.HALF_UP);
    }
    public BigDecimal getAvgWeight() {
        return countWeight == 0 ? null : sumWeight.divide(BigDecimal.valueOf(countWeight), 2, RoundingMode.HALF_UP);
    }
}