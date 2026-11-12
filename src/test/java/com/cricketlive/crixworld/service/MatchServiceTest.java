package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchServiceTest {

    @Test
    void fetchesLeaguesAndSeasonFixtures() {
        SportmonksClient client = mock(SportmonksClient.class);
        when(client.getLeagues()).thenReturn("{\"data\":[]}");
        when(client.getFixtures(216L)).thenReturn("{\"data\":[]}");
        MatchService service = new MatchService(client);

        assertEquals("{\"data\":[]}", service.getLeagues());
        assertEquals("{\"data\":[]}", service.getFixtures(216L));

        verify(client).getLeagues();
        verify(client).getFixtures(216L);
    }

    @Test
    void requestsPostMatchScorecardAndManOfTheMatch() {
        SportmonksClient client = mock(SportmonksClient.class);
        when(client.getFixture(12345L, "runs,batting,bowling,manofmatch,scoreboards,localteam,visitorteam,league,season,venue,tosswon"))
                .thenReturn("{\"data\":{}}");
        MatchService service = new MatchService(client);

        assertEquals("{\"data\":{}}", service.getPostMatch(12345L));

        verify(client).getFixture(
                12345L,
                "runs,batting,bowling,manofmatch,scoreboards,localteam,visitorteam,league,season,venue,tosswon"
        );
    }
}
