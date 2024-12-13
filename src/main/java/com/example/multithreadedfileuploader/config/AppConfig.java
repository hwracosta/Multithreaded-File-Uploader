package com.example.multithreadedfileuploader.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The AppConfig class is a configuration file for the Spring framework.
 *
 * <p>This class is responsible for specifying the base packages to scan
 * for Spring-managed components and enabling JPA repositories. It ensures
 * that the application's components and repositories are registered and managed
 * by the Spring container.</p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Defines the base package where Spring should look for beans and components.</li>
 *   <li>Enables JPA repository support for database operations.</li>
 * </ul>
 *
 * <h2>Important Annotations</h2>
 * <ul>
 *   <li><b>@Configuration</b>: Marks this class as a configuration class that provides bean definitions.</li>
 *   <li><b>@ComponentScan</b>: Specifies the base package for scanning Spring components.</li>
 *   <li><b>@EnableJpaRepositories</b>: Enables JPA repository support, allowing interaction with the database through JPA.</li>
 * </ul>
 */
@Configuration
@ComponentScan(basePackages = "com.example.multithreadedfileuploader")
@EnableJpaRepositories(basePackages = "com.example.multithreadedfileuploader.repository")
public class AppConfig {
    // This class does not define methods or properties.
    // It serves as a configuration class for the Spring application context.
}
