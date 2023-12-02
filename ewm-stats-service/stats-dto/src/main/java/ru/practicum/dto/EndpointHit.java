package ru.practicum.dto;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * EndpointHit DTO. User request information
 * Long id : identifier of the request
 * String app : identifier(name) of the service for which information is recorded
 * String uri : URI for which the request was made
 * String ip : IP address of the user who made the request
 * String timestamp : date and time when the request to the endpoint was made (format "yyyy-MM-dd HH:mm:ss")
 */
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class EndpointHit {

    Long id;
    @NotNull
    @Size(max = 64)
    String app;
    @NotNull
    @Size(max = 255)
    String uri;
    @NotNull
    @Size(max = 32)
    String ip;
    @NotNull
    String timestamp;

}
