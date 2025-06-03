package com.service.api;

import com.service.api.helpers.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class APIService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(APIService.class);

    @Override
    public void run(String... args) {
        try {
            String url = "http://example.com"; // Replace with a valid URL
            String payload = "{}"; // Replace with a valid payload or empty JSON object
            HttpHelper.sendHttpRequest(url, payload);
        } catch (IOException e) {
            logger.error("Exception Uploading File", e);
        }
    }
}
