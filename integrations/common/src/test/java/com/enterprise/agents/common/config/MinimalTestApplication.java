package com.enterprise.agents.common.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration
// Ensure components like services and repositories from the module's base package are scanned
@ComponentScan(basePackages = "com.enterprise.agents.common")
public class MinimalTestApplication {
}

