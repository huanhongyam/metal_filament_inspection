package com.kunpeng.metal_filament_inspection.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 邮箱
     */
    private String email;
}
