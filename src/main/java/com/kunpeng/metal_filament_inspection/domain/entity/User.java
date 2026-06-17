package com.kunpeng.metal_filament_inspection.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {
    

    private Long id;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（BCrypt加密）
     */
    private String password;
    
    /**
     * 角色ID (0: 普通用户, 1: 管理员)
     */
    @Builder.Default
    private Integer roleId = 0;
    
    /**
     * 用户状态 (0: 正常, 1: 禁用)
     */
    @Builder.Default
    private Integer status = 0;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 更新时间
     */
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();
} 