package com.kunpeng.metal_filament_inspection.domain.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormDTO {

    /**
     * 账户（用户名或邮箱）
     */
    @NotBlank(message = "账户不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String passwd;

    /**
     * 记住我（可选）
     */
    private Boolean remember;
}