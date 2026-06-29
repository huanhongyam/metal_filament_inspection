package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
public class PageDTO<T> {
    private List<T> records;
    private Long total;
    private Integer currentPage;
    private Integer PageSize;
}