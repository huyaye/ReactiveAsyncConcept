package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig {
    @Bean
    WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            // 워커 스레드 풀 설정을 해주지 않으면 core 갯수만큼만 워커 스레드가 생긴다.
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
                te.setCorePoolSize(100);
                te.setQueueCapacity(50);
                te.setMaxPoolSize(200);
                te.setKeepAliveSeconds(10);
                te.setThreadNamePrefix("workThread");
                te.initialize();
                configurer.setTaskExecutor(te);
            }
        };
    }
}
