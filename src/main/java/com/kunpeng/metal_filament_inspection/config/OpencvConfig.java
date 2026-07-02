package com.kunpeng.metal_filament_inspection.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "opencv")
public class OpencvConfig {
    private String libraryPath;
    @PostConstruct
    public void loadOpenCV() {
        String path = this.libraryPath;
        try {
            if (path != null && !path.trim().isEmpty()) {
                // 使用绝对路径加载
                System.load(path);
            } else {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            }
        } catch (UnsatisfiedLinkError e) {
            log.warn("OpenCV 库未安装，YOLO 检测不可用");
        }
    }
}
