package com.cricketlive.crixworld.controller;

import com.cricketlive.crixworld.service.LiveScoreService;
import com.cricketlive.crixworld.dto.LiveScorecardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cricket")
@Tag(name = "Cricket", description = "Live cricket data")
public class LiveScoreController {

    private final LiveScoreService liveScoreService;

    public LiveScoreController(LiveScoreService liveScoreService) {
        this.liveScoreService = liveScoreService;
    }

    @GetMapping(value = "/live", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get live matches", description = "Returns current live cricket scores from Sportmonks.")
    public String getLiveMatches() {
        return liveScoreService.getLiveScores();
    }

    @GetMapping(value = "/livescores", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get live scores", description = "Returns current live fixtures without included relationships.")
    public String getLiveScores() {
        return liveScoreService.getLiveScoresWithoutIncludes();
    }

    @GetMapping(value = "/live/detailed", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get detailed live scores",
            description = "Returns live fixtures with ball-by-ball events, innings totals, player statistics, officials, and match details."
    )
    public String getDetailedLiveScores() {
        return liveScoreService.getDetailedLiveScores();
    }

    @GetMapping(value = "/live/scorecard", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get live scorecards",
            description = "Returns a compact, consumer-friendly scorecard with innings totals, batting and bowling figures, and team details."
    )
    public LiveScorecardResponse getLiveScorecards() {
        return liveScoreService.getLiveScorecards();
    }

    @GetMapping(value = "/livescores", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get live scores", description = "Returns current live fixtures without included relationships.")
    public String getLiveScores() {
        return liveScoreService.getLiveScoresWithoutIncludes();  // Changed this line
    }
}