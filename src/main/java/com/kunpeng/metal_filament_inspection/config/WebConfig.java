package com.kunpeng.metal_filament_inspection.config;

import com.kunpeng.metal_filament_inspection.interceptor.JwtTokenInterceptor;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/api/user/**")
                .addPathPatterns("/api/scenario/**")
                .addPathPatterns("/api/device/**")
                .addPathPatterns("/api/wire-material/**")
                .addPathPatterns("/api/question/**")
                .addPathPatterns("/api/detection-batch/**")
                .addPathPatterns("/hi")           // 拦截请求测试
                .excludePathPatterns("/static/**",
                        "/error",
                        "/api/user/login",
                        "/api/user/register-user",
                        "/api/user/email",
                        "/api/user/email-login",
                        "/api/user/login-email",
                        "/api/wire-material/info-with-detection"); // 排除
    }
}
