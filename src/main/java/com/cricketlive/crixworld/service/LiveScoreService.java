package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import com.cricketlive.crixworld.dto.LiveScorecardResponse;
import com.cricketlive.crixworld.dto.LiveScorecardResponse.Batter;
import com.cricketlive.crixworld.dto.LiveScorecardResponse.Bowler;
import com.cricketlive.crixworld.dto.LiveScorecardResponse.Innings;
import com.cricketlive.crixworld.dto.LiveScorecardResponse.Match;
import com.cricketlive.crixworld.dto.LiveScorecardResponse.Team;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiveScoreService {

    private final SportmonksClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LiveScoreService(SportmonksClient client) {
        this.client = client;
    }

    public String getLiveScores() {

        String include = String.join(",",
                "runs",
                "batting",
                "bowling",
                "balls",
                "lineup",
                "venue",
                "tosswon",
                "localteam",
                "visitorteam"
        );

        return client.getLiveScores(include);
    }

    public String getLiveScoresWithoutIncludes() {
        return client.getLiveScores();
    }

    public String getDetailedLiveScores() {
        String include = String.join(",",
                "balls",
                "runs",
                "bowling",
                "batting",
                "scoreboards",
                "firstumpire",
                "secondumpire",
                "manofmatch",
                "tosswon",
                "manofseries",
                "visitorTeam"
        );

        return client.getLiveScores(include);
    }

    public LiveScorecardResponse getLiveScorecards() {
        String include = String.join(",",
                "runs",
                "batting",
                "bowling",
                "scoreboards",
                "localteam",
                "visitorteam",
                "tosswon"
        );

        return mapLiveScorecards(client.getLiveScores(include));
    }

    private LiveScorecardResponse mapLiveScorecards(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.hasNonNull("status") && "unavailable".equals(root.path("status").asText())) {
                throw new IllegalStateException(root.path("message").asText("Live score service is unavailable"));
            }

            JsonNode data = root.path("data");
            if (!data.isArray()) {
                throw new IllegalStateException("Sportmonks live score response did not contain a data array");
            }

            List<Match> matches = new ArrayList<>();
            for (JsonNode fixture : data) {
                matches.add(mapMatch(fixture));
            }
            return new LiveScorecardResponse(matches);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse Sportmonks live score response", exception);
        }
    }

    private Match mapMatch(JsonNode fixture) {
        Team localTeam = mapTeam(firstPresent(fixture, "localteam", "localTeam"));
        Team visitorTeam = mapTeam(firstPresent(fixture, "visitorteam", "visitorTeam"));
        List<Innings> innings = new ArrayList<>();

        JsonNode runs = fixture.path("runs");
        if (runs.isArray()) {
            for (JsonNode run : runs) {
                String scoreboard = text(run, "scoreboard");
                Long battingTeamId = longValue(run, "team_id");
                int legalBalls = legalBalls(text(run, "overs"));
                List<Batter> batters = mapBatters(fixture.path("batting"), scoreboard, battingTeamId);
                List<Bowler> bowlers = mapBowlers(fixture.path("bowling"), scoreboard, battingTeamId);

                innings.add(new Innings(
                        integer(run, "inning"),
                        scoreboard,
                        battingTeamId,
                        teamName(battingTeamId, localTeam, visitorTeam),
                        integer(run, "score"),
                        integer(run, "wickets"),
                        text(run, "overs"),
                        legalBalls,
                        runRate(integer(run, "score"), legalBalls),
                        batters,
                        bowlers
                ));
            }
        }

        JsonNode toss = fixture.path("tosswon");
        return new Match(
                longValue(fixture, "id"),
                matchName(localTeam, visitorTeam),
                text(fixture, "type"),
                text(fixture, "status"),
                fixture.path("live").asBoolean(false),
                text(fixture, "starting_at"),
                text(fixture, "note"),
                localTeam,
                visitorTeam,
                text(toss, "name"),
                text(fixture, "elected"),
                integer(fixture, "rpc_target"),
                innings
        );
    }

    private List<Batter> mapBatters(JsonNode batting, String scoreboard, Long teamId) {
        List<Batter> batters = new ArrayList<>();
        if (!batting.isArray()) {
            return batters;
        }

        for (JsonNode batter : batting) {
            if (same(text(batter, "scoreboard"), scoreboard)
                    && same(longValue(batter, "team_id"), teamId)) {
                batters.add(new Batter(
                        longValue(batter, "player_id"),
                        integer(batter, "score"),
                        integer(batter, "ball"),
                        integer(batter, "four_x"),
                        integer(batter, "six_x"),
                        decimal(batter, "rate"),
                        nullableBoolean(batter, "active")
                ));
            }
        }
        return batters;
    }

    private List<Bowler> mapBowlers(JsonNode bowling, String scoreboard, Long battingTeamId) {
        List<Bowler> bowlers = new ArrayList<>();
        if (!bowling.isArray()) {
            return bowlers;
        }

        for (JsonNode bowler : bowling) {
            if (same(text(bowler, "scoreboard"), scoreboard)
                    && !same(longValue(bowler, "team_id"), battingTeamId)) {
                bowlers.add(new Bowler(
                        longValue(bowler, "player_id"),
                        text(bowler, "overs"),
                        integer(bowler, "medians"),
                        integer(bowler, "runs"),
                        integer(bowler, "wickets"),
                        integer(bowler, "wide"),
                        integer(bowler, "noball"),
                        decimal(bowler, "rate")
                ));
            }
        }
        return bowlers;
    }

    private Team mapTeam(JsonNode team) {
        if (team == null || team.isMissingNode() || team.isNull()) {
            return null;
        }
        return new Team(
                longValue(team, "id"),
                text(team, "name"),
                text(team, "code"),
                text(team, "image_path")
        );
    }

    private JsonNode firstPresent(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.path(name);
            if (!value.isMissingNode() && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private String matchName(Team localTeam, Team visitorTeam) {
        if (localTeam == null || visitorTeam == null) {
            return null;
        }
        return localTeam.name() + " vs " + visitorTeam.name();
    }

    private String teamName(Long teamId, Team localTeam, Team visitorTeam) {
        if (localTeam != null && same(teamId, localTeam.id())) {
            return localTeam.name();
        }
        if (visitorTeam != null && same(teamId, visitorTeam.id())) {
            return visitorTeam.name();
        }
        return null;
    }

    private int legalBalls(String overs) {
        if (overs == null || overs.isBlank()) {
            return 0;
        }
        try {
            BigDecimal value = new BigDecimal(overs);
            int wholeOvers = value.intValue();
            int balls = value.subtract(BigDecimal.valueOf(wholeOvers))
                    .movePointRight(1)
                    .intValue();
            if (balls < 0 || balls > 5) {
                return 0;
            }
            return wholeOvers * 6 + balls;
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private Double runRate(Integer score, int legalBalls) {
        if (score == null || legalBalls == 0) {
            return null;
        }
        return BigDecimal.valueOf(score * 6.0 / legalBalls)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.longValue() : null;
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.intValue() : null;
    }

    private Double decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.doubleValue() : null;
    }

    private Boolean nullableBoolean(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isBoolean() ? value.booleanValue() : null;
    }

    private boolean same(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }
}