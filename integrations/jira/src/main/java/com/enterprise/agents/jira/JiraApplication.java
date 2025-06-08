package com.enterprise.agents.jira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.enterprise.agents.jira.model", "com.enterprise.agents.common.model"})
@EnableJpaRepositories(basePackages = {"com.enterprise.agents.jira.repository", "com.enterprise.agents.common.repository"})
@ComponentScan(basePackages = {"com.enterprise.agents.jira", "com.enterprise.agents.common"})
public class JiraApplication {
    public static void main(String[] args) {
        SpringApplication.run(JiraApplication.class, args);
    }
} 