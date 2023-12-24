package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utils.constants.Constants.*;

/**
 * Processing HTTP-requests to the endpoints "/hits" and "/stats" to save and receive viewing statistics data
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService service;

    /**
     * Processing POST-request to endpoint "/hit"
     * Save info about users request to the uri of a specific service.
     *
     * @param requestData contains service name, uri and user ip address
     * @return user request data with assigned id and information about saving time
     */
    @PostMapping(HIT_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit saveRequestData(@Valid @RequestBody EndpointHit requestData) {
        log.info("Save information about user request: {}", requestData);
        return service.saveRequestData(requestData);
    }

    /**
     * Processing GET-request to the endpoint "/stats"
     * Get info about users requests to the uri of a specific service.
     *
     * @param start   : date and time of the start of the range for which statistics need to be downloaded
     *                (format "yyyy-MM-dd HH:mm:ss")
     * @param end:    date and time of the start of the range for which statistics need to be downloaded
     *                (format "yyyy-MM-dd HH:mm:ss")
     * @param uris:   list of uri for which statistics need to be downloaded
     * @param unique: should only unique visits be taken into account (only with a unique ip), default value: false
     * @return list of users requests, met specified criteria
     */

    @GetMapping(STATS_PATH)
    public List<ViewStats> getStatistics(@Valid @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                         @RequestParam(
                                                 START_PARAMETER_NAME) LocalDateTime start,
                                         @Valid @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                         @RequestParam(
                                                 END_PARAMETER_NAME) LocalDateTime end,
                                         @RequestParam(
                                                 name = URIS_PARAMETER_NAME,
                                                 required = false) List<String> uris,
                                         @RequestParam(
                                                 name = UNIQUE_PARAMETER_NAME,
                                                 defaultValue = FALSE_DEFAULT_VALUE) Boolean unique) {
        log.info("Get statistic from {} to {} by uris: {}, unique: {}", start, end, uris, unique);
        return service.getStatistics(start, end, uris, unique);
    }

}
