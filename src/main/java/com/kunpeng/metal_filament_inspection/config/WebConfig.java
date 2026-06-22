package com.kunpeng.metal_filament_inspection.config;

import com.kunpeng.metal_filament_inspection.interceptor.JwtTokenInterceptor;
import com.kunpeng.metal_filament_inspection.utils.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;

    public WebConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtTokenInterceptor(jwtUtil))
                .addPathPatterns("/api/user/**")
                .addPathPatterns("/hi")           // 拦截请求测试
                .excludePathPatterns("/static/**", "/error"); // 排除静态资源和错误页
    }
}
