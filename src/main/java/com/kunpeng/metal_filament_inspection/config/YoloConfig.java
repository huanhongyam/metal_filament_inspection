package com.kunpeng.metal_filament_inspection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yolo")
public class YoloConfig {
    private String modelPath;
    private float confidenceThreshold = 0.5f;
    private float nmsThreshold = 0.4f;
    private int inputWidth = 640;
    private int inputHeight = 640;
}