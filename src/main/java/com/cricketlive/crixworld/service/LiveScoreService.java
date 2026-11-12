package com.cricketlive.crixworld.service;

import com.cricketlive.crixworld.client.SportmonksClient;
import com.cricketlive.crixworld.dto.LiveScorecardResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "liveScores", key = "#include != null ? #include : 'default'", unless = "#result == null")
    public String getLiveScores(String include) {
        return client.getLiveScores(include);
    }

    @Cacheable(value = "liveScores", key = "'no-include'", unless = "#result == null")
    public String getLiveScoresWithoutIncludes() {
        return client.getLiveScores();
    }

    @Cacheable(value = "liveScores", key = "'detailed'", unless = "#result == null")
    public String getDetailedLiveScores() {
        String include = String.join(",",
                "balls", "runs", "bowling", "batting", "scoreboards",
                "firstumpire", "secondumpire", "manofmatch", "tosswon",
                "manofseries", "visitorTeam"
        );
        return client.getLiveScores(include);
    }

    public LiveScorecardResponse getLiveScorecards() {
        String include = String.join(",",
                "runs", "batting", "bowling", "scoreboards",
                "localteam", "visitorteam", "tosswon"
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
            List<LiveScorecardResponse.Match> matches = new ArrayList<>();
            for (JsonNode fixture : data) {
                matches.add(mapMatch(fixture));
            }
            return new LiveScorecardResponse(matches);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse Sportmonks live score response", exception);
        }
    }

    private LiveScorecardResponse.Match mapMatch(JsonNode fixture) {
        LiveScorecardResponse.Team localTeam = mapTeam(firstPresent(fixture, "localteam", "localTeam"));
        LiveScorecardResponse.Team visitorTeam = mapTeam(firstPresent(fixture, "visitorteam", "visitorTeam"));
        List<LiveScorecardResponse.Innings> innings = new ArrayList<>();
        JsonNode runs = fixture.path("runs");
        if (runs.isArray()) {
            for (JsonNode run : runs) {
                String scoreboard = text(run, "scoreboard");
                Long battingTeamId = longValue(run, "team_id");
                int legalBalls = legalBalls(text(run, "overs"));
                List<LiveScorecardResponse.Batter> batters = mapBatters(fixture.path("batting"), scoreboard, battingTeamId);
                List<LiveScorecardResponse.Bowler> bowlers = mapBowlers(fixture.path("bowling"), scoreboard, battingTeamId);
                innings.add(new LiveScorecardResponse.Innings(
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
        return new LiveScorecardResponse.Match(
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

    private List<LiveScorecardResponse.Batter> mapBatters(JsonNode batting, String scoreboard, Long teamId) {
        List<LiveScorecardResponse.Batter> batters = new ArrayList<>();
        if (batting.isArray()) {
            for (JsonNode b : batting) {
                if (longValue(b, "team_id") != null && longValue(b, "team_id").equals(teamId)
                        && scoreboard.equals(text(b, "scoreboard"))) {
                    batters.add(new LiveScorecardResponse.Batter(
                            longValue(b, "player_id"),
                            text(b, "player_name"),
                            integer(b, "runs"),
                            integer(b, "balls"),
                            integer(b, "fours"),
                            integer(b, "sixes"),
                            text(b, "dismissal")
                    ));
                }
            }
        }
        return batters;
    }

    private List<LiveScorecardResponse.Bowler> mapBowlers(JsonNode bowling, String scoreboard, Long teamId) {
        List<LiveScorecardResponse.Bowler> bowlers = new ArrayList<>();
        if (bowling.isArray()) {
            for (JsonNode b : bowling) {
                if (longValue(b, "team_id") != null && !longValue(b, "team_id").equals(teamId)
                        && scoreboard.equals(text(b, "scoreboard"))) {
                    bowlers.add(new LiveScorecardResponse.Bowler(
                            longValue(b, "player_id"),
                            text(b, "player_name"),
                            text(b, "overs"),
                            integer(b, "maidens"),
                            integer(b, "runs"),
                            integer(b, "wickets"),
                            text(b, "economy")
                    ));
                }
            }
        }
        return bowlers;
    }

    private LiveScorecardResponse.Team mapTeam(JsonNode team) {
        return new LiveScorecardResponse.Team(
                longValue(team, "id"),
                text(team, "name"),
                text(team, "short_code"),
                text(team, "logo")
        );
    }

    private JsonNode firstPresent(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode child = node.path(name);
            if (!child.isMissingNode()) return child;
        }
        return node;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asLong();
    }

    private int legalBalls(String overs) {
        if (overs == null || overs.isBlank()) return 0;
        String[] parts = overs.split("\\.");
        int fullOvers = Integer.parseInt(parts[0]);
        int extraBalls = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return (fullOvers * 6) + extraBalls;
    }

    private BigDecimal runRate(Integer score, int legalBalls) {
        if (score == null || legalBalls == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(score)
                .multiply(BigDecimal.valueOf(6))
                .divide(BigDecimal.valueOf(legalBalls), 2, RoundingMode.HALF_UP);
    }

    private String matchName(LiveScorecardResponse.Team local, LiveScorecardResponse.Team visitor) {
        String localName = local != null && local.name() != null ? local.name() : "TBA";
        String visitorName = visitor != null && visitor.name() != null ? visitor.name() : "TBA";
        return localName + " vs " + visitorName;
    }

    private String teamName(Long teamId, LiveScorecardResponse.Team local, LiveScorecardResponse.Team visitor) {
        if (local != null && teamId != null && teamId.equals(local.id())) return local.name();
        if (visitor != null && teamId != null && teamId.equals(visitor.id())) return visitor.name();
        return "Unknown";
    }
}