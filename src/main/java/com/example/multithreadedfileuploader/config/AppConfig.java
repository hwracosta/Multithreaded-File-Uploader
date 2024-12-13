package com.example.multithreadedfileuploader.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.example.multithreadedfileuploader")
@EnableJpaRepositories(basePackages = "com.example.multithreadedfileuploader.repository")
public class AppConfig {
}
