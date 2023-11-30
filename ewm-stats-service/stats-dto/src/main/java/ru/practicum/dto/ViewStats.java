package ru.practicum.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * ViewStats DTO. Obtaining visitor statistics data
 * String app : identifier(name) of the service
 * String uri : URI for which the request was made
 * Integer hits : number of views
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class ViewStats {

    String app;
    String uri;
    Integer hits;

}
