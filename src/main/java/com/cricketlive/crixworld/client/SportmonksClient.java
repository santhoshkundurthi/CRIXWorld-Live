package com.cricketlive.crixworld.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SportmonksClient {

    private static final Logger log = LoggerFactory.getLogger(SportmonksClient.class);
    private static final String TOKEN_MISSING_RESPONSE =
            "{\"status\":\"unavailable\",\"message\":\"Sportmonks API token is not configured\"}";

    private final RestClient restClient;
    private final String apiToken;

    public SportmonksClient(
            RestClient sportmonksRestClient,
            @Value("${sportmonks.api-token:}") String apiToken) {

        this.restClient = sportmonksRestClient;
        this.apiToken = apiToken;
    }

    public boolean isConfigured() {
        return apiToken != null && !apiToken.isBlank();
    }

    public String getLiveScores(String include) {
        return fetchLiveScores(include);
    }

    public String getLiveScores() {
        return fetchLiveScores(null);
    }

    private String fetchLiveScores(String include) {
        if (!isConfigured()) {
            log.warn("Skipping Sportmonks live scores request because the API token is not configured.");
            return TOKEN_MISSING_RESPONSE;
        }

        return restClient.get().uri(uriBuilder -> {
                    uriBuilder.path("/livescores")
                            .queryParam("api_token", apiToken);
                    if (include != null && !include.isBlank()) {
                        uriBuilder.queryParam("include", include);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(String.class);
    }

    public String getFixture(Long fixtureId, String include) {
        if (!isConfigured()) {
            log.warn("Skipping Sportmonks fixture request because the API token is not configured.");
            return TOKEN_MISSING_RESPONSE;
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures/{id}")
                        .queryParam("api_token", apiToken)
                        .queryParam("include", include)
                        .build(fixtureId))
                .retrieve()
                .body(String.class);
    }

    public String getFixtures(Long seasonId) {
        if (!isConfigured()) {
            log.warn("Skipping Sportmonks fixtures request because the API token is not configured.");
            return TOKEN_MISSING_RESPONSE;
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fixtures")
                        .queryParam("api_token", apiToken)
                        .queryParam("filters", "seasonId:" + seasonId)
                        .build())
                .retrieve()
                .body(String.class);
    }

    public String getLeagues() {
        if (!isConfigured()) {
            log.warn("Skipping Sportmonks leagues request because the API token is not configured.");
            return TOKEN_MISSING_RESPONSE;
        }

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/leagues")
                        .queryParam("api_token", apiToken)
                        .build())
                .retrieve()
                .body(String.class);
    }
}