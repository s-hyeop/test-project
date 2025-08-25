package com.example.test_project.config.jooq;

import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {
    @Bean
    public DefaultConfigurationCustomizer jooqDefaultConfigurationCustomizer() {
        return c -> c.settings()
            .withRenderFormatted(true) // 로그에 쿼리 예쁘게 출력하기
            .withRenderSchema(false); // 로그에 스키마 빼고 출력하기
    }
}
