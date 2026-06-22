package com.kunpeng.metal_filament_inspection.aop;

import com.kunpeng.metal_filament_inspection.annotation.RequireAdmin;
import com.kunpeng.metal_filament_inspection.utils.BusinessException;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AdminAuthAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthAspect.class);

    @Around("@annotation(RequireAdmin)")
    public Object checkAdmin(ProceedingJoinPoint pjp) throws Throwable {
        Integer roleId = UserHolder.getRoleId();
        if (roleId == null || roleId != 1) {
            log.warn("用户 {} 尝试访问管理员接口，权限不足", UserHolder.getUserId());
            throw new BusinessException("权限不足，仅管理员可操作");
        }
        return pjp.proceed();
    }
}