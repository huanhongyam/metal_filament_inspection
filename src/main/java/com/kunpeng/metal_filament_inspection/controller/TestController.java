package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/hi1")
    public Result testApi(){
        return new Result("测试接口 hello World");
    }

}
