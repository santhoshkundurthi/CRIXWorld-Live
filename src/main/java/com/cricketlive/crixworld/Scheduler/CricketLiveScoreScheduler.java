package com.cricketlive.crixworld.scheduler;

import com.cricketlive.crixworld.client.SportmonksClient;
import com.cricketlive.crixworld.service.LiveScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CricketLiveScoreScheduler {

    private static final Logger log = LoggerFactory.getLogger(CricketLiveScoreScheduler.class);

    private final LiveScoreService service;
    private final SportmonksClient client;

    public CricketLiveScoreScheduler(LiveScoreService service, SportmonksClient client) {
        this.service = service;
        this.client = client;
    }

    @Scheduled(fixedDelay = 10_000)
    public void updateLiveScores() {
        if (!client.isConfigured()) {
            log.info("Skipping scheduled live score refresh because the Sportmonks API token is not configured.");
            return;
        }
        log.info("Fetching live cricket scores...");
        service.getLiveScoresWithoutIncludes();
    }
}
