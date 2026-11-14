package com.disasterrelief.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS Configuration for Two-Device Client-Server Architecture.
 * Allows Device 1 (Frontend) on the local Wi-Fi / LAN to invoke REST endpoints
 * on Device 2 (Backend) across origins and ports without cross-origin blocking.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
