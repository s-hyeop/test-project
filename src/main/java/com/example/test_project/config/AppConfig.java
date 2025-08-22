package com.example.test_project.config;

import com.example.test_project.config.properties.AppRroperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(AppRroperties.class)
public class AppConfig {
}