package com.kunpeng.metal_filament_inspection.config;

import com.huaweicloud.sdk.core.auth.AbstractCredentials;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
public class IoTAppClientConfig {

    @Value("${huawei.iot.app.ak}")
    private String ak;

    @Value("${huawei.iot.app.sk}")
    private String sk;

    @Value("${huawei.iot.app.project-id}")
    private String projectId;

    @Value("${huawei.iot.app.endpoint}")
    private String endpoint;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(ak) || !StringUtils.hasText(sk)) {
            throw new IllegalStateException("华为云 AK/SK 未配置，请检查 application.yml");
        }
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalStateException("华为云 ProjectId 未配置，请检查 application.yml");
        }
        log.info("华为云 IoTDA 客户端初始化配置校验通过");
        log.info("AK: {}, SK: {}, ProjectId: {}, Endpoint: {}",
                ak.substring(0, 4) + "****",
                sk.substring(0, 4) + "****",
                projectId,
                endpoint);
    }

    @Bean
    public IoTDAClient ioTDAClient() {
        ICredential auth = new BasicCredentials()
                .withAk(ak)
                .withSk(sk)
                .withProjectId(projectId)
                .withDerivedPredicate(AbstractCredentials.DEFAULT_DERIVED_PREDICATE);

        return IoTDAClient.newBuilder()
                .withCredential(auth)
                .withRegion(new com.huaweicloud.sdk.core.region.Region("cn-east-3", endpoint))
                .build();
    }
}