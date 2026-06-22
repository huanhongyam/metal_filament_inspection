package com.kunpeng.metal_filament_inspection.utils;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        return Result.error(e.getMessage());
    }
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e){
        return Result.error(e.getCode(),e.getMessage());
    }
}
