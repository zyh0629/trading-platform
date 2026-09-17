package com.campus.trading.config;

import com.campus.trading.utils.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:D:/upload/}")
    private String uploadPath;

    // ========== 跨域配置 ==========
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ========== 拦截器配置 ==========
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = new ArrayList<>();
        excludePaths.add("/api/user/register");
        excludePaths.add("/api/user/login");
        excludePaths.add("/api/user/security-question");
        excludePaths.add("/api/user/reset-by-security");
        excludePaths.add("/api/user/reset");
        excludePaths.add("/api/product/list");
        excludePaths.add("/api/product/**");
        excludePaths.add("/upload/**");
        excludePaths.add("/swagger-ui/**");
        excludePaths.add("/v3/api-docs/**");
        excludePaths.add("/swagger-resources/**");

        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(excludePaths);
    }

    // ========== 静态资源映射 ==========
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }
}