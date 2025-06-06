package com.enterprise.agents.common.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.enterprise.agents")
@EntityScan(basePackages = "com.enterprise.agents")
@EnableJpaRepositories(basePackages = "com.enterprise.agents")
public class TestConfig {
} 