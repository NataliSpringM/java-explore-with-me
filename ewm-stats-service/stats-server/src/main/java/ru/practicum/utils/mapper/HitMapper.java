package ru.practicum.utils.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EndpointHit;
import ru.practicum.entities.Hit;

import java.time.LocalDateTime;

import static ru.practicum.utils.constants.Constants.DATE_TIME_FORMATTER;

@UtilityClass
public class HitMapper {

    public static Hit toHitEntity(EndpointHit hit) {
        return Hit.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(LocalDateTime.parse(hit.getTimestamp(), DATE_TIME_FORMATTER))
                .build();
    }

    public static EndpointHit toEndpointHit(Hit hit) {
        return EndpointHit.builder()
                .id(hit.getId())
                .app(hit.getApp())
                .uri(hit.getUri())
                .ip(hit.getIp())
                .timestamp(hit.getTimestamp().format(DATE_TIME_FORMATTER))
                .build();
    }
}