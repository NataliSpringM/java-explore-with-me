package ru.practicum.dto;

import lombok.*;

/**
 * ViewStats DTO. Obtaining visitor statistics data
 * String app : identifier(name) of the service
 * String uri : URI for which the request was made
 * Integer hits : number of views
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ViewStats {

    String app;
    String uri;
    Long hits;

}
