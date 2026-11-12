package com.cricketlive.crixworld.request;

public record LiveMatch(
        Long id,
        String status,
        boolean live,
        String startingAt
) {
}