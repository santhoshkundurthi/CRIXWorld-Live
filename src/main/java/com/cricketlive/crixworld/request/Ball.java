package com.cricketlive.crixworld.request;

public record Ball(
        Long id,
        Integer ball,
        Long bowlerId,
        Long batsmanId,
        Integer runs,
        Boolean wicket
) {
}