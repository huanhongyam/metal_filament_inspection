package com.kunpeng.metal_filament_inspection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.kunpeng.metal_filament_inspection.mapper")
@SpringBootApplication
public class MetalFilamentInspectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetalFilamentInspectionApplication.class, args);
    }

}
