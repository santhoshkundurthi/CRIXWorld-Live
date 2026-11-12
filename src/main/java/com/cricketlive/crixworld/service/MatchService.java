package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    private final SportmonksClient client;

    public MatchService(SportmonksClient client) {
        this.client = client;
    }

    public String getLeagues() {
        return client.getLeagues();
    }

    public String getFixtures(Long seasonId) {
        return client.getFixtures(seasonId);
    }

    public String getMatch(Long fixtureId) {

        String include = String.join(",",
                "runs",
                "batting",
                "bowling",
                "balls",
                "lineup",
                "venue",
                "tosswon",
                "localteam",
                "visitorteam",
                "league",
                "season"
        );

        return client.getFixture(fixtureId, include);
    }

    public String getPostMatch(Long fixtureId) {
        String include = String.join(",",
                "runs",
                "batting",
                "bowling",
                "manofmatch",
                "scoreboards",
                "localteam",
                "visitorteam",
                "league",
                "season",
                "venue",
                "tosswon"
        );

        return client.getFixture(fixtureId, include);
    }
}