package com.fishlog.fishlog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiClientConfig {

    @Value("${ai.fishial.base-url:https://api.fishial.ai}")
    private String baseUrl;

    @Bean
    public RestClient fishialRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
