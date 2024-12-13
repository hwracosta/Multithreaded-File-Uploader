package com.example.multithreadedfileuploader.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * The DataSourceConfig class is a configuration file responsible for setting up
 * the application's data source and entity manager factory.
 *
 * <p>This class defines the database connection details and the Hibernate settings
 * for interacting with the database. It ensures that the application has the
 * necessary configurations to manage entities and perform persistence operations.</p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Configures the database connection using a {@link DataSource} bean.</li>
 *   <li>Defines the entity manager factory for managing JPA entities.</li>
 *   <li>Sets Hibernate properties to customize database interactions.</li>
 * </ul>
 *
 * <h2>Important Methods</h2>
 * <ul>
 *   <li><b>dataSource()</b>: Configures and returns the database connection settings.</li>
 *   <li><b>entityManagerFactory()</b>: Configures and returns the JPA entity manager factory.</li>
 *   <li><b>hibernateProperties()</b>: Provides Hibernate-specific settings as a map.</li>
 * </ul>
 *
 * <h2>Annotations</h2>
 * <ul>
 *   <li><b>@Configuration</b>: Indicates that this class is a Spring configuration file.</li>
 *   <li><b>@Bean</b>: Marks methods that produce Spring-managed beans.</li>
 * </ul>
 */
@Configuration
public class DataSourceConfig {

    /**
     * Configures the data source for connecting to the PostgreSQL database.
     *
     * @return A {@link DataSource} object with the connection details.
     */
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://localhost:5432/fileuploader")
                .username("postgres")
                .password("1028")
                .build();
    }

    /**
     * Configures the JPA entity manager factory for managing entities.
     *
     * @param builder The {@link EntityManagerFactoryBuilder} used to create the factory.
     * @param dataSource The configured {@link DataSource} for database connections.
     * @return A {@link LocalContainerEntityManagerFactoryBean} for managing JPA entities.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.multithreadedfileuploader.entity") // Adjust the entity package path if needed
                .persistenceUnit("default")
                .properties(hibernateProperties()) // Use a Map instead of Properties
                .build();
    }

    /**
     * Provides Hibernate-specific properties for database interactions.
     *
     * <p>This method sets properties like SQL dialect, DDL auto behavior, and SQL formatting.
     * Adjust these settings based on the environment (e.g., production vs. development).</p>
     *
     * @return A {@link Map} containing Hibernate properties.
     */
    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update"); // Use 'validate' or 'none' in production
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        return properties;
    }
}
