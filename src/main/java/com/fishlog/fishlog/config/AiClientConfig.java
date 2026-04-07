package com.fishlog.fishlog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Value("${ai.fishial.base-url:https://api-recognition.fishial.ai}")
    private String fishialBaseUrl;

    @Value("${ai.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String geminiBaseUrl;

    @Bean
    public RestClient fishialRestClient() {
        return RestClient.builder()
                .baseUrl(fishialBaseUrl)
                .build();
    }

    @Bean
    public RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(geminiBaseUrl)
                .build();
    }
}
