package com.example.test_project.config;

import com.example.test_project.config.properties.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
}