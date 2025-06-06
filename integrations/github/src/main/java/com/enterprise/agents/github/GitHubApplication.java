package com.enterprise.agents.github;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.enterprise.agents.github", "com.enterprise.agents.common"})
public class GitHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(GitHubApplication.class, args);
    }
} 