package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private Integer roleId;

}
