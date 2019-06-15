package com.lyc.TicketManager_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SessionCheckConfig implements WebMvcConfigurer {

    @Bean
    SessionInterceptor sessionInterceptor() {
        return new SessionInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor())
                .excludePathPatterns("/static/*")
                .excludePathPatterns("/error")
                .addPathPatterns("/**");
    }
}