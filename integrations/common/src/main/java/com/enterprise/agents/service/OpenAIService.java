package com.enterprise.agents.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OpenAIService {
    @Value("${openai.api.key}")
    private String openAIApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAIUrl;

    public String generateOnboardingMessage(String prompt) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        String requestBody = "{" +
                "\"model\": \"gpt-3.5-turbo\"," +
                "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]" +
                "}";
        Request request = new Request.Builder()
                .url(openAIUrl)
                .post(RequestBody.create(requestBody, mediaType))
                .addHeader("Authorization", "Bearer " + openAIApiKey)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string(); // In production, parse and extract the message
            }
        } catch (IOException e) {
            // Log error
        }
        return "Welcome! (OpenAI unavailable)";
    }
}

