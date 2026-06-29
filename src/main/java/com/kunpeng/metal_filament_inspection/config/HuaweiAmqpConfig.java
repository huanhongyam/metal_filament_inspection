package com.kunpeng.metal_filament_inspection.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.transports.TransportOptions;
import org.apache.qpid.jms.transports.TransportSupport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

@Configuration
@EnableConfigurationProperties(AmqpClientConfig.class)
public class HuaweiAmqpConfig {

    private final AmqpClientConfig options;

    public HuaweiAmqpConfig(AmqpClientConfig options) {
        this.options = options;
    }

    @Bean(name = "huaweiJmsConnectionFactory")
    public ConnectionFactory huaweiJmsConnectionFactory() throws Exception {
        String remoteUrl = String.format("%s:%d?amqp.vhost=%s&amqp.idleTimeout=8000&amqp.saslMechanisms=PLAIN",
                options.getHost(), options.getPort(), options.getInstanceId());

        JmsConnectionFactory factory = new JmsConnectionFactory(remoteUrl);

        TransportOptions transportOptions = new TransportOptions();
        transportOptions.setTrustAll(true);
        factory.setSslContext(TransportSupport.createJdkSslContext(transportOptions));

        // 动态用户名：只包含 accessKey 和 timestamp，不包含 instanceId
        factory.setExtension(
                JmsConnectionExtensions.USERNAME_OVERRIDE.toString(),
                (connection, uri) -> String.format("accessKey=%s|timestamp=%d",
                        options.getAccessKey(),
                        System.currentTimeMillis())
        );

        factory.setClientID(options.getClientId());

        // 初始用户名
        String initialUserName = String.format("accessKey=%s|timestamp=%d",
                options.getAccessKey(),
                System.currentTimeMillis());
        factory.setUsername(initialUserName);
        factory.setPassword(options.getAccessCode());

        return factory;
    }

    @Bean(name = "iotAmqpListenerFactory")
    public JmsListenerContainerFactory<?> iotAmqpListenerFactory(
            @Qualifier("huaweiJmsConnectionFactory") ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency(options.getConcurrency());
        if (options.isAutoAcknowledge()) {
            factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        } else {
            factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        }
        factory.setRecoveryInterval(5000L);
        return factory;
    }
}