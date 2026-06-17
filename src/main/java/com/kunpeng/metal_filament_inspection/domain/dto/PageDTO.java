package com.kunpeng.metal_filament_inspection.domain.dto;

import java.util.List;

public class PageDTO<T> {
    private List<T> data;
    private Long total;
    private Integer currentPage;
    private Integer PageSize;
}