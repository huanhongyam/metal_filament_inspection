package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LoginUser {
    private Long userId;
    private Integer roleId;

    public LoginUser(Long userId, Integer roleId) {
    }
}
