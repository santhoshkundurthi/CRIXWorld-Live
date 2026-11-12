package com.cricketlive.crixworld.controller;

import com.cricketlive.crixworld.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cricket")
@Tag(name = "Matches", description = "Cricket fixture details")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/leagues")
    @Operation(
            summary = "Find leagues and seasons",
            description = "Returns Sportmonks leagues and their seasons so the IPL league and active season IDs can be identified."
    )
    public String getLeagues() {
        return matchService.getLeagues();
    }

    @GetMapping("/fixtures")
    @Operation(summary = "Get season fixtures", description = "Returns the fixture schedule for the specified season.")
    public String getFixtures(
            @Parameter(description = "Sportmonks season ID", example = "216")
            @RequestParam Long seasonId) {
        return matchService.getFixtures(seasonId);
    }

    @GetMapping("/fixtures/{fixtureId}")
    @Operation(
            summary = "Get full fixture details",
            description = "Returns ball-by-ball and scorecard data for a fixture."
    )
    public String getFixtureDetails(
            @Parameter(description = "Sportmonks fixture ID", example = "12345")
            @PathVariable Long fixtureId) {
        return matchService.getMatch(fixtureId);
    }

    @GetMapping("/fixtures/{fixtureId}/summary")
    @Operation(
            summary = "Get post-match summary",
            description = "Returns the final result, scorecard, and player-of-the-match details for a fixture."
    )
    public String getPostMatch(
            @Parameter(description = "Sportmonks fixture ID", example = "12345")
            @PathVariable Long fixtureId) {
        return matchService.getPostMatch(fixtureId);
    }

    @GetMapping("/matches/{fixtureId}")
    @Operation(summary = "Get match details", description = "Returns details for a cricket fixture.")
    public String getMatch(
            @Parameter(description = "Sportmonks fixture ID", example = "12345")
            @PathVariable Long fixtureId) {
        return matchService.getMatch(fixtureId);
    }
}