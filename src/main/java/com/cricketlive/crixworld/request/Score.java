package com.cricketlive.crixworld.request;

public record Score(
        Integer runs,
        Integer wickets,
        String overs
) {
}