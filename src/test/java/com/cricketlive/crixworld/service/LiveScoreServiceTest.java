package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import com.cricketlive.crixworld.dto.LiveScorecardResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LiveScoreServiceTest {

    @Test
    void mapsSportmonksLiveScoresToCompactScorecard() {
        SportmonksClient client = mock(SportmonksClient.class);
        when(client.getLiveScores(anyString())).thenReturn("""
                {
                  "data": [{
                    "id": 71942,
                    "type": "T20",
                    "status": "2nd Innings",
                    "live": true,
                    "localteam": {"id": 60, "name": "Titans", "code": "MT"},
                    "visitorteam": {"id": 63, "name": "Warriors", "code": "WAR"},
                    "tosswon": {"name": "Warriors"},
                    "runs": [{"team_id": 60, "inning": 1, "score": 180, "wickets": 9, "overs": 20, "scoreboard": "S1"}],
                    "batting": [{"team_id": 60, "player_id": 53165, "scoreboard": "S1", "score": 10, "ball": 9, "four_x": 2, "six_x": 0, "rate": 111, "active": false}],
                    "bowling": [{"team_id": 63, "player_id": 35306, "scoreboard": "S1", "overs": 4, "medians": 0, "runs": 33, "wickets": 4, "wide": 0, "noball": 0, "rate": 8.25}]
                  }]
                }
                """);
        LiveScoreService service = new LiveScoreService(client);

        LiveScorecardResponse response = service.getLiveScorecards();

        assertEquals(1, response.matches().size());
        var match = response.matches().get(0);
        assertEquals("Titans vs Warriors", match.name());
        assertEquals("Warriors", match.tossWinner());
        assertEquals(1, match.innings().size());
        var innings = match.innings().get(0);
        assertEquals(180, innings.runs());
        assertEquals(120, innings.legalBalls());
        assertEquals(9.0, innings.runRate());
        assertEquals(53165, innings.batting().get(0).playerId());
        assertEquals(35306, innings.bowling().get(0).playerId());
        assertFalse(innings.batting().get(0).active());
    }
}
