package com.service.api;

import com.service.api.helpers.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class APIService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);

    @Override
    public void run(String... args) {
        try {
            String response = HttpHelper.sendHttpRequest("http://example.com", "{}");
            logger.info("API Service started successfully. Response: {}", response);
        } catch (Exception e) {
            logger.error("Error starting API Service", e);
        }
    }
}
