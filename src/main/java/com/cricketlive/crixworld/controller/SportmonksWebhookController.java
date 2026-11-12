package com.cricketlive.crixworld.controller;

import com.cricketlive.crixworld.service.LiveScoreUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/sportmonks")
public class SportmonksWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SportmonksWebhookController.class);
    private final LiveScoreUpdateService updateService;

    public SportmonksWebhookController(LiveScoreUpdateService updateService) {
        this.updateService = updateService;
    }

    @PostMapping("/live-score")
    public ResponseEntity<String> receiveLiveScoreUpdate(@RequestBody String payload) {
        try {
            log.info("Received webhook from Sportmonks");
            updateService.cacheLiveScore(payload);
            updateService.broadcastToNodeApps(payload);
            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}