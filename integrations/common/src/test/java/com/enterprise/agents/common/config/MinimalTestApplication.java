package com.enterprise.agents.common.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.enterprise.agents.common.repository")
@EntityScan(basePackages = "com.enterprise.agents.common.model")
// Ensure components like services and repositories from the module's base package are scanned
@ComponentScan(basePackages = "com.enterprise.agents.common")
@ActiveProfiles("test")
public class MinimalTestApplication {
}

