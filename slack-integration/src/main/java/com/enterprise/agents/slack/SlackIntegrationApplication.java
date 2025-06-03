package com.enterprise.agents.slack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.enterprise.agents.slack", "com.enterprise.agents.service"})
public class SlackIntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlackIntegrationApplication.class, args);
    }
}

