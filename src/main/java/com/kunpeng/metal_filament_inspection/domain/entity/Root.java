package com.kunpeng.metal_filament_inspection.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Root {
    
    @Id
    private Long id;
    
    /**
     * 用户名
     */
    String userName;

    /**
     * 密码(BCrypt加密)
     */
    String password;
    
}
