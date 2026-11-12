package com.cricketlive.crixworld.dto;

import java.util.List;

public record LiveScorecardResponse(List<Match> matches) {

    public record Match(
            Long fixtureId,
            String name,
            String format,
            String status,
            boolean live,
            String startingAt,
            String note,
            Team localTeam,
            Team visitorTeam,
            String tossWinner,
            String tossDecision,
            Integer target,
            List<Innings> innings) {
    }

    public record Team(Long id, String name, String code, String logoUrl) {
    }

    public record Innings(
            Integer number,
            String scoreboard,
            Long battingTeamId,
            String battingTeam,
            Integer runs,
            Integer wickets,
            String overs,
            Integer legalBalls,
            Double runRate,
            List<Batter> batting,
            List<Bowler> bowling) {
    }

    public record Batter(
            Long playerId,
            Integer runs,
            Integer ballsFaced,
            Integer fours,
            Integer sixes,
            Double strikeRate,
            Boolean active) {
    }

    public record Bowler(
            Long playerId,
            String overs,
            Integer maidens,
            Integer runsConceded,
            Integer wickets,
            Integer wides,
            Integer noBalls,
            Double economyRate) {
    }
}
