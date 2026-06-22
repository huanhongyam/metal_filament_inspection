package com.kunpeng.metal_filament_inspection.utils;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;
    private final String msg;

    // 只传错误信息（默认错误码为 500）
    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

    // 传错误码和错误信息
    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}