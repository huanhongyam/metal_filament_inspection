package com.kunpeng.metal_filament_inspection.controller;

import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "测试接口")
@RestController
public class TestController {
    @GetMapping("/hi")
    public Result testApi(){
        return new Result("测试接口 hello World");
    }
}
