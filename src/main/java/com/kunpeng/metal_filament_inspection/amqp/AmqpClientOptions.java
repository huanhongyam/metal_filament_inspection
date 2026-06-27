package com.kunpeng.metal_filament_inspection.amqp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "huawei.iot.amqp")
public class AmqpClientOptions {

    @NotBlank(message = "接入地址不能为空")
    private String host;

    private int port = 5671;

    @NotBlank(message = "accessKey 不能为空")
    private String accessKey;

    @NotBlank(message = "accessCode 不能为空")
    private String accessCode;

    @NotBlank(message = "队列名称不能为空")
    private String queueName;

    private String instanceId = "default";

    private boolean autoAcknowledge;

    private String clientId = "backend-service-1";

    /** 并发消费者数量，格式如 "3-10" */
    private String concurrency = "3-10";

    public String generateConnectUrl() {
        String url = String.format("%s:%d", host, port);
        if (StringUtils.isNotBlank(instanceId)) {
            url += "?amqp.vhost=" + instanceId;
        }
        return url;
    }
}