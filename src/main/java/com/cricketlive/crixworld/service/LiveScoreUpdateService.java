package com.cricketlive.crixworld.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LiveScoreUpdateService {

    private static final Logger log = LoggerFactory.getLogger(LiveScoreUpdateService.class);
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LiveScoreUpdateService(StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void cacheLiveScore(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode data = root.path("data");

            if (data.isArray()) {
                for (JsonNode fixture : data) {
                    String matchId = fixture.path("id").asText();
                    String redisKey = "match:" + matchId + ":score";
                    redisTemplate.opsForValue().set(redisKey, fixture.toString(), 24, TimeUnit.HOURS);
                    log.debug("Cached score for match {} in Redis", matchId);
                }
            }
        } catch (Exception e) {
            log.error("Error caching live score", e);
        }
    }

    public void broadcastToNodeApps(String payload) {
        try {
            messagingTemplate.convertAndSend("/topic/live-scores", payload);
            log.debug("Broadcasted live score to WebSocket subscribers");
        } catch (Exception e) {
            log.error("Error broadcasting to WebSocket", e);
        }
    }

    public String getScoreFromCache(String matchId) {
        return redisTemplate.opsForValue().get("match:" + matchId + ":score");
    }
}