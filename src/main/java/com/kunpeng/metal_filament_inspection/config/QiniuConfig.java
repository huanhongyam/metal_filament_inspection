package com.kunpeng.metal_filament_inspection.config;

import com.qiniu.storage.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qiniu")
public class QiniuConfig {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain;
    private String region;
    private Long expireSeconds;
    private Boolean useHttps;

    /**
     * 根据配置的 region 字符串返回对应的七牛 Region 对象
     * 如果未配置或无法识别，则返回 Region.autoRegion() 自动探测
     */
    public Region getRegion() {
        if (region == null || region.trim().isEmpty()) {
            return Region.autoRegion();
        }
        return Region.autoRegion(); // 自动探测

    }
}