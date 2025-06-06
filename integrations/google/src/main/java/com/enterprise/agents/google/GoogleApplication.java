package com.enterprise.agents.google;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.enterprise.agents.google", "com.enterprise.agents.common"})
public class GoogleApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoogleApplication.class, args);
    }
} 