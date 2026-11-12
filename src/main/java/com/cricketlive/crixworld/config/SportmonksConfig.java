package com.cricketlive.crixworld.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SportmonksConfig {

    @Bean
    public RestClient sportmonksRestClient(
            @Value("${sportmonks.base-url:https://cricket.sportmonks.com/api/v2.0}") String baseUrl) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}