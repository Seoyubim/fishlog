package com.fishlog.fishlog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AiClientConfig {

    @Value("${ai.fish-classification.base-url}")
    private String baseUrl;

    @Value("${ai.fish-classification.api-key:}")
    private String apiKey;

    @Value("${ai.fish-classification.timeout-seconds:10}")
    private int timeoutSeconds;

    @Bean
    public RestClient aiRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }
}
