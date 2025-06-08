package com.enterprise.agents.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.enterprise.agents.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    @Configuration
    @Profile("dev")
    @ConditionalOnProperty(name = "spring.datasource.url", havingValue = "jdbc:h2:mem:testdb")
    public static class H2Config {
        // H2 specific configuration if needed
    }

    @Configuration
    @Profile("prod")
    @ConditionalOnProperty(name = "spring.datasource.url", matchIfMissing = false)
    public static class ProductionConfig {
        // Production database specific configuration
    }
} 