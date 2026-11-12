package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final SportmonksClient client;

    public MatchService(SportmonksClient client) {
        this.client = client;
    }

    @Cacheable(value = "leagues", key = "'all'", unless = "#result == null")
    public String getLeagues() {
        return client.getLeagues();
    }

    @Cacheable(value = "fixtures", key = "#seasonId", unless = "#result == null")
    public String getFixtures(Long seasonId) {
        return client.getFixtures(seasonId);
    }

    @Cacheable(value = "matchDetails", key = "#fixtureId", unless = "#result == null")
    public String getMatch(Long fixtureId) {
        String include = String.join(",",
                "runs", "batting", "bowling", "balls", "lineup",
                "venue", "tosswon", "localteam", "visitorteam", "league", "season"
        );
        return client.getFixture(fixtureId, include);
    }

    @Cacheable(value = "postMatch", key = "#fixtureId", unless = "#result == null")
    public String getPostMatch(Long fixtureId) {
        String include = String.join(",",
                "runs", "batting", "bowling", "manofmatch", "scoreboards",
                "localteam", "visitorteam", "league", "season", "venue", "tosswon"
        );
        return client.getFixture(fixtureId, include);
    }
}